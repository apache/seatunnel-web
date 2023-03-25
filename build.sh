#!/bin/sh

set -
WORKDIR=$(
  cd "$(dirname "$0")" || exit
  pwd
)

DOCKER_VERSION=1.0.0-snapshot

# build code
code() {
  /bin/sh $WORKDIR/mvnw clean package -DskipTests
  # mv release zip
  mv $WORKDIR/seatunnel-server/seatunnel-app/target/seatunnel-web.zip $WORKDIR/
}

# build image
image() {
  docker buildx build --load --no-cache -t apache/seatunnel-web:$DOCKER_VERSION -t apache/seatunnel-web:latest -f $WORKDIR/docker/backend.dockerfile .
}

# main
case "$1" in
"code")
  code
  ;;
"image")
  image
  ;;
*)
  echo "Usage: seatunnel-daemon.sh {start|stop|status}"
  exit 1
  ;;
esac
set +
