## Local development (Linux/WSL/macOS only)

### IDE

- install `detekt` plugin in IntelliJ IDEA. Set config in `Preferences -> Tools -> detekt -> Configuration file` to `detekt.yml`

### Run via docker

Add to `.env` file:
```sh
JWT_SECRET_ACCESS=0LHQvtC20LUg0L/QvtC80L7Qs9C4INC90LDQvCDQt9Cw0LrRgNGL0YLRjCDRjdGC0L7RgiDQv9GA0LXQtNC80LXRgg==

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

## Kafka

Просмотр лидера кластера
```bash
docker compose exec kafka-1 kafka-metadata-shell.sh \
  --snapshot /bitnami/kafka/data/__cluster_metadata-0/00000000000000000000.log \
  cat /metadataQuorum/leader
```

Проверка доступности данных на другой реплике
```bash
docker compose exec kafka-2 bash
$ kafka-console-consumer.sh --topic events --bootstrap-server localhost:9092 --from-beginning
```

## Progress

- [x] Lab1 complete
- [x] Lab2 complete
- [ ] Lab3 in progress
