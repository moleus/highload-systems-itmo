ARG MIGRATIONS_DIR

FROM liquibase/liquibase:4.29

COPY $MIGRATIONS_DIR /liquibase/changelog
