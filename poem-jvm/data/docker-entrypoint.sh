#!/bin/bash
set -e

# Directory for database data
DATADIR="/var/lib/postgresql/data"

# Ensure the data directory has the correct owner
chown -R postgres:postgres "$DATADIR"

# Initialize the database if the data directory is empty
if [ ! -s "$DATADIR/PG_VERSION" ]; then
    echo "Initializing database directory..."
    initdb -D "$DATADIR"

    # Start PostgreSQL temporarily
    echo "Starting PostgreSQL server for initialization..."
    pg_ctl -D "$DATADIR" -o "-c listen_addresses=''" -w start

    echo "Running initialization scripts..."
    for file in /docker-entrypoint-initdb.d/*; do
        case "$file" in
            *.sql)    echo "Executing $file"; psql -U postgres -f "$file";;
            *.sh)     echo "Running $file"; bash "$file";;
            *)        echo "Ignoring $file";;
        esac
    done

    # Stop PostgreSQL after initialization
    echo "Stopping PostgreSQL server after initialization..."
    pg_ctl -D "$DATADIR" -m fast -w stop
else
    echo "Database directory already initialized. Skipping initialization."
fi

# Start PostgreSQL server
exec postgres -D "$DATADIR"
