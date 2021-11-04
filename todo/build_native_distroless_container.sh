#!/bin/sh
CONTAINER_NAME=todo
TAG=native-distroless
TYPE=native-distroless
CONTAINER_RUNTIME=podman
podman --version 1>/dev/null 2>&1
if [ $? -ne 0 ];
then
   CONTAINER_RUNTIME=docker 
fi
# Use native container build
START_BUILD_APP=$(date +%s)
mvn clean package -Pnative \
-Dquarkus.native.remote-container-build=true \
-Dquarkus.native.container-runtime=podman \
-Dquarkus.native.native-image-xmx=5g
END_BUILD_APP=$(date +%s)
START_BUILD_CONTAINER=$(date +%s)
$CONTAINER_RUNTIME build -f src/main/docker/Dockerfile.$TYPE \
-t ${CONTAINER_NAME}:${TAG} .
END_BUILD_CONTAINER=$(date +%s)
BUILD_APP=$(expr ${END_BUILD_APP} - ${START_BUILD_APP})
BUILD_CONTAINER=$(expr ${END_BUILD_CONTAINER} - ${START_BUILD_CONTAINER})
echo "Elasped time to build app:${BUILD_APP} sec"
echo "Elasped time to build container:${BUILD_CONTAINER} sec"

