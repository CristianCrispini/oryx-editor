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
    # Process numbered files by incrementing index
    index=1
    while true; do
        file=$(find /docker-entrypoint-initdb.d -type f -name "${index}_*.sql" -o -name "${index}_*.sh" | head -n 1)
        if [[ -z "$file" ]]; then
            break  # Exit loop if no file matches the current index
        fi

        case "$file" in
            *.sql) echo "Executing $file"; psql -U postgres -f "$file";;
            *.sh)  echo "Running $file"; bash "$file";;
            *)     echo "Ignoring $file";;
        esac

        index=$((index + 1))
    done

    # Stop PostgreSQL after initialization
    echo "Stopping PostgreSQL server after initialization..."
    pg_ctl -D "$DATADIR" -m fast -w stop
else
    echo "Database directory already initialized. Skipping initialization."
fi

# Start PostgreSQL server
exec postgres -D "$DATADIR"
