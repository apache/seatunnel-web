#!/bin/sh

set -
# usage: seatunnel-backend-daemon.sh <start|stop|status>

WORKDIR=$(cd "$(dirname "$0")" || exit; pwd)

# start
start() {
  echo "starting seatunnel..."

  JAVA_OPTS="${JAVA_OPTS} -server -Xms1g -Xmx1g -Xmn512m -XX:+PrintGCDetails -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof"
  SPRING_OPTS="${SPRING_OPTS} -Dspring.config.name=application.yml -Dspring.config.location=classpath:application.yml"

  # check env JAVA_HOME
  if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set"
    exit 1
  fi

  echo "$WORKDIR"
  $JAVA_HOME/bin/java $JAVA_OPTS \
  -cp "$WORKDIR/../conf":"$WORKDIR/../libs/*" \
  $SPRING_OPTS \
  org.apache.seatunnel.app.SeatunnelApplication
  echo "seatunnel started"
}
# stop
stop() {
  echo "stopping seatunnel..."
  pid=$(jcmd | grep -i 'seatunnel-app-.*jar' | grep -v grep | awk '{print $1}')
  if [ -n "$pid" ]; then
    kill -15 $pid
    echo "seatunnel stopped"
  else
    echo "seatunnel is not running"
  fi
}

#status
status() {
  pid=$(jcmd | grep -i 'seatunnel-app-.*jar' | grep -v grep | awk '{print $1}')
  if [ -n "$pid" ]; then
    echo "seatunnel is running"
  else
    echo "seatunnel is not running"
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