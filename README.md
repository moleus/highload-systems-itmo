# Liquibase

Для написания файлов миграций используется YAML.

Структура:

- resources:
  - db:
    - changelog:
      - changeset:
        - \<somechange1\>-\<table1\>.yaml
        - \<somechange2\>-\<table2\>.yaml
    - db.changelog-master.yaml
