#!/bin/bash
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

#This script is used to download the connector plug-ins required during the running process.
#All are downloaded by default. You can also choose what you need.
#You only need to configure the plug-in name in config/plugin_config.

# get seatunnel web home
SEATUNNEL_WEB_HOME=$(cd $(dirname $0);cd ../;pwd)
DATASOURCE_DIR=${SEATUNNEL_WEB_HOME}/datasource

# If you don’t want to download a certain data source, you can delete the element below
datasource_list=(
  "datasource-plugins-api"
  "datasource-elasticsearch"
  "datasource-hive"
  "datasource-jdbc-clickhouse"
  "datasource-jdbc-hive"
  "datasource-jdbc-mysql"
  "datasource-jdbc-oracle"
  "datasource-jdbc-postgresql"
  "datasource-jdbc-redshift"
  "datasource-jdbc-sqlserver"
  "datasource-jdbc-starrocks"
  "datasource-jdbc-tidb"
  "datasource-kafka"
  "datasource-mysql-cdc"
  "datasource-s3"
  "datasource-sqlserver-cdc"
  "datasource-starrocks"
  "datasource-mongodb"
)

# the datasource default version is 1.0.0, you can also choose a custom version. eg: 1.1.2:  sh install-datasource.sh 2.1.2
version=1.0.0

if [ -n "$1" ]; then
    version="$1"
fi

echo "Downloading SeaTunnel Web Datasource lib, usage version is ${version}"

# create the datasource directory
if [ ! -d "$DATASOURCE_DIR" ];
  then
      mkdir -p "$DATASOURCE_DIR"
      echo "Created datasource directory."
fi

for i in "${datasource_list[@]}"
do
	echo "$i"
	echo "Downloading datasource: " "$i"
  "$SEATUNNEL_WEB_HOME"/mvnw dependency:get -DgroupId=org.apache.seatunnel -DartifactId="$i" -Dversion="$version" -Ddest="$DATASOURCE_DIR"
done
