#!/usr/bin/env bash
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 \
    --name todo-db -e POSTGRES_USER=todo \
    -e POSTGRES_PASSWORD=todoPassword -e POSTGRES_DB=todo \
    -p 5432:5432 postgres:13.1
