#!/bin/sh

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -
WORKDIR=$(
  cd "$(dirname "$0")" || exit
  pwd
)

PROJECT_VERSION=$(/bin/sh ${WORKDIR}/mvnw help:evaluate -Dexpression=project.version -q -DforceStdout | tail -1)
IMAGE_VERSION=${IMAGE_VERSION:-"${PROJECT_VERSION}"}

# build code
build_dist() {
  echo "Build dist version ${PROJECT_VERSION}"
  /bin/sh $WORKDIR/mvnw clean package -DskipTests
  # mv release zip
  mv $WORKDIR/seatunnel-server/seatunnel-app/target/seatunnel-web.zip $WORKDIR/
}

# build image
build_image() {
  echo "Building image version ${IMAGE_VERSION}"
  echo docker buildx build --load --no-cache -t apache/seatunnel-web:${IMAGE_VERSION} -t apache/seatunnel-web:latest -f $WORKDIR/docker/backend.dockerfile .
}

print_help() {
  echo "Usage: build.sh {dist|image}"
  echo "Use this script to build an archived binary or a docker image"
  echo ""
  echo "Arguments:"
  echo "          dist    Build an archived distribution and place the binary file in project home directory"
  echo "          image   Build a docker image."
}

# main
case "$1" in
"dist")
  build_dist
  ;;
"image")
  build_image
  ;;
*)
  print_help
  exit 1
  ;;
esac
set +
