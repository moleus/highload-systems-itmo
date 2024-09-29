## Local development (Linux/WSL/macOS only)

### IDE

- install `detekt` plugin in IntelliJ IDEA. Set config in `Preferences -> Tools -> detekt -> Configuration file` to `detekt.yml`

### Run via docker

Add to `.env` file:
```sh
JWT_SECRET_ACCESS=0LHQvtC20LUg0L/QvtC80L7Qs9C4INC90LDQvCDQt9Cw0LrRgNGL0YLRjCDRjdGC0L7RgiDQv9GA0LXQtNC80LXRgg==
JWT_SECRET_REFRESH=0LfQsNGH0LXQvCDRgtGLINGN0YLQviDRh9C40YLQsNC10YjRjCDRjdGC0L4g0LLQvtC+0LHRidC1INGC0L4g0KHQldCa0KDQldCi
JWT_EXPIRATION_ACCESS=60
JWT_EXPIRATION_REFRESH=30

# optionally
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/run/user/501/docker.sock
DOCKER_HOST=/var/run/docker.sock
```

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
