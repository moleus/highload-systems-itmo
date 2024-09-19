## Local development (Linux/WSL/macOS only)

### IDE

- install `detekt` plugin in IntelliJ IDEA. Set config in `Preferences -> Tools -> detekt -> Configuration file` to `detekt.yml`

### Run via docker

build App + run App and PostgresqlDB:
```sh
. ./.env
gradle jibDockerBuild && docker-compose up
```

### Run DB in docker and App in IDE

run only db in docker:
```sh
docker-compose up db
```


## Liquibase

Для написания файлов миграций используется YAML.

Структура:

- resources:
  - db:
    - changelog:
      - changeset:
        - \<somechange1\>-\<table1\>.yaml
        - \<somechange2\>-\<table2\>.yaml
    - db.changelog-master.yaml
