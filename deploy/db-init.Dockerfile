ARG MIGRATIONS_DIR

FROM liquibase/liquibase:4.29

COPY $MIGRATION_DIR /liquibase/changelog
