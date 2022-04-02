#!/bin/bash
#docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 \
CONTAINER_RUNTIME=podman
podman --version 1>/dev/null 2>&1
if [ $? -ne 0 ];
then
   CONTAINER_RUNTIME=docker 
fi
#$CONTAINER_RUNTIME run --ulimit memlock=-1:-1 -it --rm=true  \
$CONTAINER_RUNTIME run  --rm=true  \
    --name todo-db -e POSTGRES_USER=todo \
    -e POSTGRES_PASSWORD=todoPassw0rd -e POSTGRES_DB=todo \
    -p 5432:5432 postgres:13.1
