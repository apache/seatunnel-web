#!/usr/bin/env bash
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

# Home directory of spark distribution.
SPARK_HOME=${SPARK_HOME:-/opt/spark}
# Home directory of flink distribution.
FLINK_HOME=${FLINK_HOME:-/opt/flink}
# Below variables are used in seatunnel-cluster.sh, seatunnel.sh, seatunnel-connector.sh etc scripts
STACK_ROOT=${STACK_ROOT:-"/usr/bigtop"}
export APP_DIR="$STACK_ROOT/current/seatunnel"
export CONF_DIR="/etc/seatunnel/conf"
export LOG_DIR="/var/log/seatunnel"
