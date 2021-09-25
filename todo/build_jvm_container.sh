#!/bin/bash
CONTAINER_NAME=todo
TAG=v1
TYPE=fast-jar
mvn clean package -DskipTests=true -Dquarkus.package.type=$TYPE
docker build -f src/main/docker/Dockerfile.$TYPE \
-t ${CONTAINER_NAME}:${TAG} .
