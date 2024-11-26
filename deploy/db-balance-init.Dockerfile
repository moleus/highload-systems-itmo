FROM liquibase/liquibase:4.29
ARG MIGRATIONS_DIR

COPY $MIGRATIONS_DIR /liquibase/changelog-balance
