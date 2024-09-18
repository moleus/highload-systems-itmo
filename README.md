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
