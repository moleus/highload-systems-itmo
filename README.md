## Local development

- build App
- run App & PostgresqlDB
```sh
. ./.env
gradle jibDockerBuild && docker-compose up
```

### Run only DB
```sh
docker-compose up db
```

## CI pipeline

### Tests

Github Actions setup postgresql service and run tests