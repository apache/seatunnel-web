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

# Default directories
STACK_ROOT=${STACK_ROOT:-"/usr/bigtop"}
export LOGDIR=${LOGDIR:-"/var/log/seatunnel-web"}
export CONFDIR=${CONFDIR:-"/etc/seatunnel-web/conf"}
export SEATUNNEL_WEB_HOME=${SEATUNNEL_WEB_HOME:-"$STACK_ROOT/current/seatunnel-web"}
export SEATUNNEL_HOME="${STACK_ROOT}/current/seatunnel"

# JVM options
export JAVA_MEMORY_OPTS=${JAVA_MEMORY_OPTS:-"-Xms2g -Xmx4g -Xmn1g"}
export JAVA_GC_OPTS=${JAVA_GC_OPTS:-"-XX:+PrintGCDetails -Xloggc:${LOGDIR}/gc.log"}
export JAVA_ERROR_OPTS=${JAVA_ERROR_OPTS:-"-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}/dump.hprof"}

#JDK17_OPTS="--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.security.jgss/sun.security.krb5=ALL-UNNAMED"
#JAVA_OPTS="$JAVA_OPTS $JDK17_OPTS"

#DEBUG_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=*:8476,suspend=n"
#DEBUG_OPTS="$DEBUG_OPTS -verbose:class"
#export JAVA_OPTS="$JAVA_OPTS $DEBUG_OPTS"

