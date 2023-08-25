/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

DROP TABLE IF EXISTS "user_login_log";
CREATE TABLE "user_login_log" (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_id INT NOT NULL,
                                  token CLOB NOT NULL,
                                  token_status TINYINT NOT NULL,
                                  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS role;
CREATE TABLE role (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      type INT NOT NULL,
                      role_name VARCHAR(255) NOT NULL,
                      description VARCHAR(255),
                      create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------
-- Table structure for role_user_relation
-- ----------------------------
DROP TABLE IF EXISTS role_user_relation;
CREATE TABLE role_user_relation (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    role_id INT NOT NULL,
                                    user_id INT NOT NULL,
                                    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table structure for t_st_datasource
DROP TABLE IF EXISTS t_st_datasource;
CREATE TABLE t_st_datasource (
                                 id BIGINT NOT NULL,
                                 datasource_name VARCHAR(63) NOT NULL,
                                 plugin_name VARCHAR(63) NOT NULL,
                                 plugin_version VARCHAR(63) DEFAULT '1.0.0',
                                 datasource_config VARCHAR(1023) NOT NULL,
                                 description VARCHAR(63) DEFAULT NULL,
                                 create_user_id INT NOT NULL,
                                 update_user_id INT NOT NULL,
                                 create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                 update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                 PRIMARY KEY (id),
                                 UNIQUE (datasource_name)
);

-- Table structure for t_st_job_definition
DROP TABLE IF EXISTS t_st_job_definition;
CREATE TABLE t_st_job_definition (
                                     id BIGINT NOT NULL,
                                     name VARCHAR(50) NOT NULL,
                                     description VARCHAR(255) DEFAULT NULL,
                                     job_type VARCHAR(50) DEFAULT NULL,
                                     create_user_id INT NOT NULL,
                                     update_user_id INT NOT NULL,
                                     create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                     update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                     PRIMARY KEY (id),
                                     UNIQUE (name)
);

-- Table structure for t_st_job_instance
DROP TABLE IF EXISTS t_st_job_instance;
CREATE TABLE t_st_job_instance (
                                   id BIGINT NOT NULL,
                                   job_define_id BIGINT NOT NULL,
                                   job_status VARCHAR(50) DEFAULT NULL,
                                   job_config CLOB NOT NULL,
                                   engine_name VARCHAR(50) NOT NULL,
                                   engine_version VARCHAR(50) NOT NULL,
                                   job_engine_id VARCHAR(200) DEFAULT NULL,
                                   create_user_id INT NOT NULL,
                                   update_user_id INT DEFAULT NULL,
                                   create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                   update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                   end_time TIMESTAMP(3) DEFAULT NULL,
                                   job_type VARCHAR(50) NOT NULL,
                                   PRIMARY KEY (id)
);

-- Table structure for t_st_job_instance_history
DROP TABLE IF EXISTS t_st_job_instance_history;
CREATE TABLE t_st_job_instance_history (
                                           id BIGINT NOT NULL,
                                           dag CLOB NOT NULL,
                                           create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                           update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                           PRIMARY KEY (id)
);

-- Table structure for t_st_job_line
DROP TABLE IF EXISTS t_st_job_line;
CREATE TABLE t_st_job_line (
                               id BIGINT NOT NULL,
                               version_id BIGINT NOT NULL,
                               input_plugin_id VARCHAR(50) NOT NULL,
                               target_plugin_id VARCHAR(50) NOT NULL,
                               create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               PRIMARY KEY (id),
                               INDEX job_line_version_index (version_id)
);

-- Table structure for t_st_job_metrics
DROP TABLE IF EXISTS t_st_job_metrics;
CREATE TABLE t_st_job_metrics (
                                  id BIGINT NOT NULL,
                                  job_instance_id BIGINT NOT NULL,
                                  pipeline_id INT NOT NULL,
                                  read_row_count BIGINT NOT NULL,
                                  write_row_count BIGINT NOT NULL,
                                  source_table_names VARCHAR(200) DEFAULT NULL,
                                  sink_table_names VARCHAR(200) DEFAULT NULL,
                                  read_qps BIGINT DEFAULT NULL,
                                  write_qps BIGINT DEFAULT NULL,
                                  record_delay BIGINT DEFAULT NULL,
                                  status VARCHAR(20) DEFAULT NULL,
                                  create_user_id INT NOT NULL,
                                  update_user_id INT DEFAULT NULL,
                                  create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  PRIMARY KEY (id)
);

-- Table structure for t_st_job_task
DROP TABLE IF EXISTS t_st_job_task;
CREATE TABLE t_st_job_task (
                               id BIGINT NOT NULL,
                               version_id BIGINT NOT NULL,
                               plugin_id VARCHAR(50) NOT NULL,
                               name VARCHAR(50) NOT NULL,
                               config CLOB DEFAULT NULL,
                               transform_options VARCHAR(5000) DEFAULT NULL,
                               output_schema CLOB DEFAULT NULL,
                               connector_type VARCHAR(50) NOT NULL,
                               datasource_id BIGINT DEFAULT NULL,
                               datasource_option VARCHAR(5000) DEFAULT NULL,
                               select_table_fields VARCHAR(5000) DEFAULT NULL,
                               scene_mode VARCHAR(50) DEFAULT NULL,
                               type VARCHAR(50) NOT NULL,
                               create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               PRIMARY KEY (id),
                               INDEX job_task_plugin_id_index (plugin_id)
);

-- Table structure for t_st_job_version
DROP TABLE IF EXISTS t_st_job_version;
CREATE TABLE t_st_job_version (
                                  id BIGINT NOT NULL,
                                  job_id BIGINT NOT NULL,
                                  name VARCHAR(255) NOT NULL,
                                  job_mode VARCHAR(10) NOT NULL,
                                  env CLOB DEFAULT NULL,
                                  engine_name VARCHAR(50) NOT NULL,
                                  engine_version VARCHAR(50) NOT NULL,
                                  create_user_id INT NOT NULL,
                                  update_user_id INT NOT NULL,
                                  create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                  PRIMARY KEY (id)
);

-- Table structure for t_st_virtual_table
DROP TABLE IF EXISTS t_st_virtual_table;
CREATE TABLE t_st_virtual_table (
                                    id BIGINT NOT NULL,
                                    datasource_id BIGINT NOT NULL,
                                    virtual_database_name VARCHAR(63) NOT NULL,
                                    virtual_table_name VARCHAR(63) NOT NULL,
                                    table_fields VARCHAR(1023) NOT NULL,
                                    virtual_table_config VARCHAR(1023) NOT NULL,
                                    description VARCHAR(63) DEFAULT NULL,
                                    create_user_id INT NOT NULL,
                                    update_user_id INT NOT NULL,
                                    create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                    update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                    PRIMARY KEY (id)
);

-- Table structure for user
DROP TABLE IF EXISTS "user";
CREATE TABLE "user" (
                      id INT NOT NULL AUTO_INCREMENT,
                      username VARCHAR(255) NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      status TINYINT NOT NULL,
                      type TINYINT NOT NULL,
                      create_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                      update_time TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                      PRIMARY KEY (id)
);

-- Records of user
INSERT INTO "user" ("username", "password", "status", "type") VALUES ('admin', '7f97da8846fed829bb8d1fd9f8030f3b', 0, 0);

-- Records of user_login_log
-- No equivalent records provided for the user_login_log table in the provided SQL script.
-- You can insert records into this table using similar INSERT INTO statements.
-- However, you would need to provide the values for columns like "user_id", "token", "token_status", etc.
