#!/bin/sh
CONTAINER_NAME=quay.io/voravitl/todo
PLATFORM=linux/amd64,linux/arm64
TAG=${1:-multi-arch} #otel,latest,hi — pass as first arg to override
DOCKERFILE=jvm #jvm,hummingbird
IMAGE=$CONTAINER_NAME:$TAG
CONTAINER_RUNTIME=podman
podman --version 1>/dev/null 2>&1
if [ $? -ne 0 ];
then
   CONTAINER_RUNTIME=docker 
fi
# $IMAGE may already exist locally either as a manifest list (from a prior
# run of this script) or as a plain image (from a prior FAILED run, or an
# unrelated single-arch build/tag reusing the same name) — `manifest exists`
# only recognizes the former, so a stale plain image silently blocks
# `manifest create` ("that name is already in use") without the guard ever
# firing. Clear both possibilities unconditionally.
$CONTAINER_RUNTIME rmi $IMAGE >/dev/null 2>&1
$CONTAINER_RUNTIME manifest rm $IMAGE >/dev/null 2>&1
$CONTAINER_RUNTIME manifest create $IMAGE
if [ $? -ne 0 ];
then
   echo "manifest create failed, aborting"
   exit 1
fi
echo "Build with Dockerfile.$DOCKERFILE tag $TAG"
MVN_PROFILE=""
if [ "$TAG" = "otel" ];
then
   MVN_PROFILE="-Dquarkus.profile=otel"
fi
mvn clean package -DskipTests=true $MVN_PROFILE
if [ $? -ne 0 ];
then
   echo "Maven build failed, aborting"
   exit 1
fi
$CONTAINER_RUNTIME build --platform $PLATFORM  --manifest \
$IMAGE -f src/main/docker/Dockerfile.$DOCKERFILE  .
$CONTAINER_RUNTIME manifest push $IMAGE
if [ $? -ne 0 ];
then
    echo "manifest push failed, aborting"
    exit 1
fi
# Verify against the registry (not local podman state) via skopeo --raw:
# proves the push actually landed a multi-arch index, not just that the
# local build succeeded.
ARCH=$(skopeo inspect --raw docker://${CONTAINER_NAME}:${TAG} | jq -r '.manifests[].platform.architecture')
printf "${CONTAINER_NAME}:${TAG} architectures (verified on registry):\n%s\n" "$ARCH"
printf "**** Don't manually push this tag with podman push ****"
# #!/bin/sh
# CONTAINER_NAME=quay.io/voravitl/todo
# PLATFORM=linux/amd64,linux/arm64
# TAG=multi-arch
# #TAG=multi-arch-fips
# DOCKERFILE=hummingbird
# #DOCKERFILE=hummingbird-fips
# IMAGE=$CONTAINER_NAME:$TAG
# podman manifest exists $IMAGE 
# if [ $? -eq 0 ];
# then
#  podman manifest rm $IMAGE
# fi
# podman manifest create $IMAGE 
# echo "Build with Dockerfile.$DOCKERFILE tag $TAG"
# mvn clean package -DskipTests=true
# CONTAINER_RUNTIME=podman
# podman --version 1>/dev/null 2>&1
# if [ $? -ne 0 ];
# then
#    CONTAINER_RUNTIME=docker 
# fi
# $CONTAINER_RUNTIME build --platform $PLATFORM  --manifest \
# $IMAGE -f src/main/docker/Dockerfile.$DOCKERFILE  .
# $CONTAINER_RUNTIME manifest push $IMAGE
# ARCH=$($CONTAINER_RUNTIME manifest inspect ${CONTAINER_NAME}:${TAG} | jq -r '.manifests[].platform.architecture')
# printf "${CONTAINER_NAME}:${TAG} architectures:\n\r $ARCH"
# # if [ $? -eq 0 ];
# # then
# #    ARCH=$($CONTAINER_RUNTIME inspect ${CONTAINER_NAME}:${TAG} | jq '.[0].Architecture' | sed 's/\"//g')
# #    printf "${CONTAINER_NAME}:${TAG} architecture is $ARCH"
# # fi
