#!/bin/sh
CONTAINER_NAME=todo
TAG=otel-native
TYPE=multistage
CONTAINER_RUNTIME=podman
podman --version 1>/dev/null 2>&1
if [ $? -ne 0 ];
then
   CONTAINER_RUNTIME=docker 
fi
START_BUILD_CONTAINER=$(date +%s)
$CONTAINER_RUNTIME build -f src/main/docker/Dockerfile.$TYPE \
-t ${CONTAINER_NAME}:${TAG} .
END_BUILD_CONTAINER=$(date +%s)
BUILD_CONTAINER=$(expr ${END_BUILD_CONTAINER} - ${START_BUILD_CONTAINER})
echo "Elasped time to build container:${BUILD_CONTAINER} sec"
