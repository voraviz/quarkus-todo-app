#!/bin/sh
CONTAINER_NAME=todo
TAG=native
TYPE=native
#TAG=native-distroless
#TYPE=native-distroless
# Use native container build
START_BUILD_APP=$(date +%s)
mvn clean package -Dquarkus.native.container-build=true \
-DskipTests=true  -Pnative
END_BUILD_APP=$(date +%s)
START_BUILD_CONTAINER=$(date +%s)
docker build -f src/main/docker/Dockerfile.$TYPE \
-t ${CONTAINER_NAME}:${TAG} .
END_BUILD_CONTAINER=$(date +%s)
BUILD_APP=$(expr ${END_BUILD_APP} - ${START_BUILD_APP})
BUILD_CONTAINER=$(expr ${END_BUILD_CONTAINER} - ${START_BUILD_CONTAINER})
echo "Elasped time to build app:${BUILD_APP} sec"
echo "Elasped time to build container:${BUILD_CONTAINER} sec"
