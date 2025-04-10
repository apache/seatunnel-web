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
# usage: seatunnel-backend-daemon.sh <start|stop|status>

WORKDIR=$(cd "$(dirname "$0")" || exit; pwd)

# check
check() {
  # check whether the SEATUNNEL_HOME exists or not.
  if [ "${SEATUNNEL_HOME}" ];then
  	echo "Load connectors from ${SEATUNNEL_HOME}"
  else
  	echo "SEATUNNEL_HOME is not be set. Please check it."
  	exit 1
  fi
}

# start
start() {
  echo "starting seatunnel-web..."

  check

  LOGDIR=${WORKDIR}/../logs
  # Create the log directory if it does not exist
  if [ ! -d "$LOGDIR" ]; then
    mkdir -p "$LOGDIR"
  fi
  JAVA_OPTS="${JAVA_OPTS} -server -Xms1g -Xmx1g -Xmn512m -XX:+PrintGCDetails -Xloggc:${LOGDIR}/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof"
  SPRING_OPTS="${SPRING_OPTS} -Dspring.config.name=application.yml -Dspring.config.location=classpath:application.yml"
  JAVA_OPTS="${JAVA_OPTS} -Dseatunnel-web.logs.path=${LOGDIR}"
  # check env JAVA_HOME
  if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set"
    exit 1
  fi

  echo "$WORKDIR"
  CLASSPATH="$WORKDIR/../conf:$WORKDIR/../libs/*:$WORKDIR/../datasource/*"
  if [ -d "$WORKDIR/../ranger-seatunnel-plugin" ]; then
  CLASSPATH="$CLASSPATH:$WORKDIR/../ranger-seatunnel-plugin/lib/*.jar"
  CLASSPATH="$CLASSPATH:$WORKDIR/../ranger-seatunnel-plugin/lib/ranger-seatunnel-plugin-impl/*"
  fi

  nohup $JAVA_HOME/bin/java $JAVA_OPTS \
  -cp "$CLASSPATH" $SPRING_OPTS \
  org.apache.seatunnel.app.SeatunnelApplication >> "${LOGDIR}/seatunnel.out" 2>&1 &
  echo "seatunnel-web started"
}
# stop
stop() {
  echo "stopping seatunnel-web..."
  pid=$(jcmd | grep -i 'org.apache.seatunnel.app.SeatunnelApplication' | grep -v grep | awk '{print $1}')
  if [ -n "$pid" ]; then
    kill -15 $pid
    echo "seatunnel-web stopped"
  else
    echo "seatunnel-web is not running"
  fi
}

#status
status() {
  pid=$(jcmd | grep -i 'org.apache.seatunnel.app.SeatunnelApplication' | grep -v grep | awk '{print $1}')
  if [ -n "$pid" ]; then
    echo "seatunnel-web is running"
  else
    echo "seatunnel-web is not running"
  fi
}

# main
case "$1" in
"start")
  start
  ;;
"stop")
  stop
  ;;
"status")
  status
  ;;
*)
  echo "Usage: seatunnel-daemon.sh {start|stop|status}"
  exit 1
esac
