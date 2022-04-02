#!/bin/bash
CONTAINER_NAME=todo-reactive
TAG=v1
TYPE=jvm
CONTAINER_RUNTIME=podman
podman --version 1>/dev/null 2>&1
if [ $? -ne 0 ];
then
   CONTAINER_RUNTIME=docker 
fi
mvn clean package -DskipTests=true -Dquarkus.package.type=fast-jar
$CONTAINER_RUNTIME build -f src/main/docker/Dockerfile.$TYPE \
-t ${CONTAINER_NAME}:${TAG} .
