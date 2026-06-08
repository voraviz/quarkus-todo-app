#!/bin/bash
START_BUILD_APP=$(date +%s)
IMAGE=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:25.0.3.0-Final-java25-amd64
mvn clean package -DskipTests -Dnative -Dquarkus.native.container-build=true \
-Dquarkus.native.builder-image=$IMAGE
END_BUILD_APP=$(date +%s)
BUILD_APP_TIME=$(expr ${END_BUILD_APP} - ${START_BUILD_APP})
BUILD_APP_TIME=$(expr ${BUILD_APP_TIME} / 60 )
echo "Elasped time to build container ${BUILD_APP_TIME} minutes"
