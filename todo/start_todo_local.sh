#!/bin/bash
./target/quarkus-todo-apps-1.0.0-runner \
-Dquarkus.datasource.db-kind=postgresql \
-Dquarkus.datasource.username=todo \
-Dquarkus.datasource.password=todoPassw0rd \
-Dquarkus.datasource.jdbc.url=jdbc:postgresql://127.0.0.1:5432/todo \
-Dquarkus.hibernate-orm.database.generation=drop-and-create
