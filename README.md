## Local development (Linux/WSL/macOS only)

### IDE

- install `detekt` plugin in IntelliJ IDEA. Set config in `Preferences -> Tools -> detekt -> Configuration file` to `detekt.yml`

### Run via docker

build App + run App and PostgresqlDB:
```sh
. ./.env
gradle jibDockerBuild 
docker-compose up
```

### Run DB in docker and App in IDE

run only db in docker:
```sh
docker-compose up db
```


## Liquibase

Для написания файлов миграций используется YAML.

При изменении схемы БД нужно:
- создать директорию changelog-<version> в resources/db/changelog 
- добавить в нее файлы с изменениями (например, tables.yaml, procedures.yaml)

Структура:

```
- resources:
  - db:
    - changelog-root.yaml
    - changelog:
      - changelog-1.0:
        - tables.yaml
        - procedures.yaml
        - ...
      - changelog-1.1:
        - tables.yaml
        - procedures.yaml
      - ...
```
