FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV SHELL=/bin/bash
ARG POSTGRES_VERSION=15

RUN apt-get update && apt-get -y dist-upgrade && \
  apt-get install -y --no-install-recommends \
    wget curl gnupg2 ca-certificates gdal-bin sudo vim lsb-release && \
  sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list' && \
  wget -qO- https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo tee /etc/apt/trusted.gpg.d/postgresql.asc && \
  apt-get update && apt-get install -y --no-install-recommends \
    postgresql-${POSTGRES_VERSION} postgresql-${POSTGRES_VERSION}-postgis-3 postgresql-${POSTGRES_VERSION}-pgrouting postgis && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/*

RUN service postgresql start && \
  sudo -u postgres psql -c 'create database gis;' && \
  sudo -u postgres psql -d gis -c 'create extension postgis;' && \
  sudo -u postgres psql -d gis -c 'create extension postgis_raster;' && \
  sudo -u postgres psql -d gis -c 'create extension postgis_sfcgal;' && \
  sudo -u postgres psql -d gis -c 'create extension postgis_topology;' && \
  sudo -u postgres psql -d gis -c "SET postgis.gdal_enabled_drivers = 'ENABLE_ALL';" && \
  sudo -u postgres psql -d gis -c 'create extension pgrouting;' && \
  sudo -u postgres psql -c 'create user gisuser;' && \
  sudo -u postgres psql -c "alter user gisuser with encrypted password 'gisuser';" && \
  sudo -u postgres psql -c 'grant all privileges on database gis to gisuser;' && \
  printf "\tlisten_addresses='*'\t" >> /etc/postgresql/${POSTGRES_VERSION}/main/postgresql.conf && \
  sed -i -E '/local +all +all +peer/ s/peer/scram-sha-256/' /etc/postgresql/${POSTGRES_VERSION}/main/pg_hba.conf && \
  sed -i -E '/host +all +all +127.0.0.1\/32 +scram-sha-256/ s/127.0.0.1\/32/0.0.0.0\/0   /' /etc/postgresql/${POSTGRES_VERSION}/main/pg_hba.conf && \
  sed -i -E '/host +all +all +::1\/128 +scram-sha-256/ s/::1\/128/::0\/0  /' /etc/postgresql/${POSTGRES_VERSION}/main/pg_hba.conf &&\ 
  printf "localhost:5432:gis:gisuser:gisuser" >> /root/.pgpass && \
  chmod 0600 /root/.pgpass

EXPOSE 5432

CMD service postgresql start && \
  tail -f /dev/null  