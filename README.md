## Local development (Linux/WSL/macOS)

### IDE

- install `detekt` plugin in IntelliJ IDEA. Set config in `Preferences -> Tools -> detekt -> Configuration file` to `detekt.yml`

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

## K8S Deploy
```bash
cd ./deploy && bash ./deploy-all.sh
helm install -f kafka-values.yaml -n dev kafka-release oci://registry-1.docker.io/bitnamicharts/kafka
```
После изменений в вальюсах:
```bash
helm upgrade -i kafka-release -f kafka-values.yaml -n dev oci://registry-1.docker.io/bitnamicharts/kafka
```

## Progress

- [x] Lab1 complete
- [x] Lab2 complete
- [x] Lab3 in progress
