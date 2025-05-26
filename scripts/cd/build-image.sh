#
# Builds a Docker image.
#
# Environment variables:
#
#   CONTAINER_REGISTRY - The hostname of your container registry.
#   VERSION - The version number to tag the images with.
#   NAME - The name of the image to build.
#   DIRECTORY - The directory form which to build the image.
#
# Usage:
#
#       ./scripts/cd/build-image.sh
#

set -u
: "$CONTAINER_REGISTRY"
: "$VERSION"
: "$NAME"
: "$DIRECTORY"
: "$REGISTRY_UN"
: "$REGISTRY_PW"

BASE_IMAGE="$CONTAINER_REGISTRY/$NAME"
IMAGE="$BASE_IMAGE:$VERSION"

cd "$DIRECTORY"
mvn compile jib:build \
  -Djib.to.image="$IMAGE" \
  -Djib.to.auth.username="$REGISTRY_UN" \
  -Djib.to.auth.password="$REGISTRY_PW" \
  -Djib.to.tags="$VERSION,latest"