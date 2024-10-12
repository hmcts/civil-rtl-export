#!/usr/bin/env bash

set -e

# Civil Judgement Feed database
if [ -z "$JF_DB_PASSWORD" ] || [ -z "$JF_DB_USERNAME" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'JF_DB_USERNAME' and 'JF_DB_PASSWORD'."
  exit 1
fi

psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=${JF_DB_USERNAME} --set PASSWORD=${JF_DB_PASSWORD} <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD :'PASSWORD';

  CREATE DATABASE civil_rtl_export
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
