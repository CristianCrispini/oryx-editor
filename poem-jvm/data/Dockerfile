# Stage 1: Compile and Install PostgreSQL
FROM debian:bullseye AS builder

# Install dependencies for compilation
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

# Stage 2: Final Runtime Image
FROM debian:bullseye

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    libreadline8 \
    zlib1g \
    libssl1.1 \
    libxml2 \
    libxslt1.1 \
    python2.7 \
    libpython2.7 \
    ca-certificates && \
    apt-get clean

# Copy PostgreSQL binaries and libraries from the builder stage
COPY --from=builder /usr/local/ /usr/local/
COPY --from=builder /usr/lib/x86_64-linux-gnu/libpython2.7.so.1.0 /usr/lib/x86_64-linux-gnu/

# Add PostgreSQL binaries to PATH
ENV PATH="/usr/local/pgsql/bin:$PATH"

# Create a non-root user for PostgreSQL
RUN groupadd -r postgres && useradd -r -g postgres postgres

# Prepare PostgreSQL directories
RUN mkdir -p /var/lib/postgresql/data && \
    mkdir -p /docker-entrypoint-initdb.d && \
    chown -R postgres:postgres /var/lib/postgresql /docker-entrypoint-initdb.d

# Add SQL initialization scripts
RUN echo "CREATE USER poem WITH SUPERUSER;" > /docker-entrypoint-initdb.d/init.sql && \
    echo "CREATE DATABASE poem OWNER poem;" >> /docker-entrypoint-initdb.d/init.sql && \
    echo "CREATE PROCEDURAL LANGUAGE plpythonu;" > /docker-entrypoint-initdb.d/plpythonu.sql

COPY database/db_schema.sql /docker-entrypoint-initdb.d/

# Copy the entrypoint script
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Switch to the non-root user
USER postgres

# Expose PostgreSQL port
EXPOSE 5432

# Set the entrypoint
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
