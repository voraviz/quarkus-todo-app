#!/bin/bash
CONTAINER_NAME=quay.io/voravitl/todo
TAG=reactive-otel
TYPE=jvm
CONTAINER_RUNTIME=podman
podman --version 1>/dev/null 2>&1
if [ $? -ne 0 ];
then
   CONTAINER_RUNTIME=docker 
fi
#mvn clean package -DskipTests=true -Dquarkus.package.type=$TYPE
mvn clean package -DskipTests=true
$CONTAINER_RUNTIME build --platform linux/amd64 -f src/main/docker/Dockerfile.$TYPE \
-t ${CONTAINER_NAME}:${TAG} .
