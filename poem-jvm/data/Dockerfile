FROM debian:bullseye

# Install dependencies for building PostgreSQL and runtime
RUN apt-get update && apt-get install -y \
    wget \
    build-essential \
    libreadline-dev \
    zlib1g-dev \
    libssl-dev \
    libxml2-dev \
    libxslt1-dev \
    libpython2.7-dev \
    python2.7 \
    libpython2.7 \
    ca-certificates && \
    apt-get clean

# Download and compile PostgreSQL
RUN wget https://ftp.postgresql.org/pub/source/v12.14/postgresql-12.14.tar.gz && \
    tar -xvzf postgresql-12.14.tar.gz && \
    cd postgresql-12.14 && \
    ./configure --with-python PYTHON=/usr/bin/python2.7 && \
    make && \
    make install && \
    cd .. && rm -rf postgresql-12.14*

# Add PostgreSQL binaries to PATH
ENV PATH="/usr/local/pgsql/bin:$PATH"

# Create a non-root user for PostgreSQL
RUN groupadd -r postgres && useradd -r -g postgres postgres

# Prepare PostgreSQL directories
RUN mkdir -p /var/lib/postgresql/data && \
    mkdir -p /docker-entrypoint-initdb.d && \
    chown -R postgres:postgres /var/lib/postgresql /docker-entrypoint-initdb.d

# Add SQL initialization scripts
RUN echo "CREATE USER poem WITH SUPERUSER;" > /docker-entrypoint-initdb.d/1_init.sql && \
    echo "CREATE DATABASE poem OWNER poem;" >> /docker-entrypoint-initdb.d/1_init.sql && \
    echo "CREATE PROCEDURAL LANGUAGE plpythonu;" > /docker-entrypoint-initdb.d/2_pllang.sql \
    echo "CREATE PROCEDURAL LANGUAGE plpgsql;" > /docker-entrypoint-initdb.d/2_pllang.sql

    
COPY database/db_schema.sql /docker-entrypoint-initdb.d/3_db_schema.sql

# Copy the entrypoint script
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Switch to the non-root user
USER postgres

# Expose PostgreSQL port
EXPOSE 5432

# Set the entrypoint
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
