FROM postgres:15
USER postgres

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d civil_rtl_export -U ${JF_DB_USERNAME}

EXPOSE 5432
