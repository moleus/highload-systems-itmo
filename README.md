## Local development

- build App
- run App & PostgresqlDB
```sh
. ./.env
gradle jibDockerBuild && docker-compose up
```

## CI pipeline

### Tests

Github Actions setup postgresql service and 