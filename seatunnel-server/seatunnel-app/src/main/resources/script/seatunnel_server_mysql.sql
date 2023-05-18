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

CREATE
DATABASE IF NOT EXISTS seatunnel;


-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`role`;
CREATE TABLE `seatunnel`.`role`
(
    `id`          int(20) NOT NULL AUTO_INCREMENT,
    `type`        int(2) NOT NULL,
    `role_name`   varchar(255) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of role
-- ----------------------------

-- ----------------------------
-- Table structure for role_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`role_user_relation`;
CREATE TABLE `seatunnel`.`role_user_relation`
(
    `id`          int(20) NOT NULL AUTO_INCREMENT,
    `role_id`     int(20) NOT NULL,
    `user_id`     int(20) NOT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of role_user_relation
-- ----------------------------

-- ----------------------------
-- Table structure for scheduler_config
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`scheduler_config`;
CREATE TABLE `seatunnel`.`scheduler_config`
(
    `id`                 int(11) NOT NULL AUTO_INCREMENT,
    `script_id`          int(11) DEFAULT NULL,
    `trigger_expression` varchar(255) DEFAULT NULL,
    `retry_times`        int(11) NOT NULL DEFAULT '0',
    `retry_interval`     int(11) NOT NULL DEFAULT '0',
    `active_start_time`  datetime(3) NOT NULL,
    `active_end_time`    datetime(3) NOT NULL,
    `create_time`        datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3),
    `update_time`        datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    `creator_id`         int(11) NOT NULL,
    `update_id`          int(11) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- ----------------------------
-- Records of scheduler_config
-- ----------------------------

-- ----------------------------
-- Table structure for script
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`script`;
CREATE TABLE `seatunnel`.`script`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `name`        varchar(255) NOT NULL,
    `type`        tinyint(4) NOT NULL,
    `status`      tinyint(4) NOT NULL,
    `content`     mediumtext,
    `content_md5` varchar(255) DEFAULT NULL,
    `creator_id`  int(11) NOT NULL,
    `mender_id`   int(11) NOT NULL,
    `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP (3),
    `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of script
-- ----------------------------

-- ----------------------------
-- Table structure for script_job_apply
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`script_job_apply`;
CREATE TABLE `seatunnel`.`script_job_apply`
(
    `id`                  int(11) NOT NULL AUTO_INCREMENT,
    `script_id`           int(11) NOT NULL,
    `scheduler_config_id` int(11) NOT NULL,
    `job_id`              bigint(20) DEFAULT NULL,
    `operator_id`         int(11) NOT NULL,
    `create_time`         datetime(3) DEFAULT CURRENT_TIMESTAMP (3),
    `update_time`         datetime(3) DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of script_job_apply
-- ----------------------------

-- ----------------------------
-- Table structure for script_param
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`script_param`;
CREATE TABLE `seatunnel`.`script_param`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `script_id`   int(11) DEFAULT NULL,
    `key`         varchar(255) NOT NULL,
    `value`       varchar(255) DEFAULT NULL,
    `status`      tinyint(4) DEFAULT NULL,
    `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP (3),
    `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of script_param
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`user`;
CREATE TABLE `seatunnel`.`user`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `username`    varchar(255) NOT NULL,
    `password`    varchar(255) NOT NULL,
    `status`      tinyint(4) NOT NULL,
    `type`        tinyint(4) NOT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`user_login_log`;
CREATE TABLE `seatunnel`.`user_login_log`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id`      int(11) NOT NULL,
    `token`        mediumtext NOT NULL,
    `token_status` tinyint(1) NOT NULL,
    `create_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3),
    `update_time`  datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) ON UPDATE CURRENT_TIMESTAMP (3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of user_login_log
-- ----------------------------

INSERT INTO `seatunnel`.`user`(`username`, `password`, `status`, `type`)
values ('admin', '7f97da8846fed829bb8d1fd9f8030f3b', 0, 0);



-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_ds_task_definition`;
CREATE TABLE `seatunnel`.`t_ds_task_definition` (
                                        `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
                                        `code` bigint(20) NOT NULL COMMENT 'encoding',
                                        `name` varchar(200) DEFAULT NULL COMMENT 'task definition name',
                                        `version` int(11) DEFAULT '0' COMMENT 'task definition version',
                                        `description` text COMMENT 'description',
                                        `project_code` bigint(20) NOT NULL COMMENT 'project code',
                                        `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
                                        `task_type` varchar(50) NOT NULL COMMENT 'task type',
                                        `task_params` longtext COMMENT 'job custom parameters',
                                        `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
                                        `task_priority` tinyint(4) DEFAULT '2' COMMENT 'job priority',
                                        `worker_group` varchar(200) DEFAULT NULL COMMENT 'worker grouping',
                                        `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                        `fail_retry_times` int(11) DEFAULT NULL COMMENT 'number of failed retries',
                                        `fail_retry_interval` int(11) DEFAULT NULL COMMENT 'failed retry interval',
                                        `timeout_flag` tinyint(2) DEFAULT '0' COMMENT 'timeout flag:0 close, 1 open',
                                        `timeout_notify_strategy` tinyint(4) DEFAULT NULL COMMENT 'timeout notification policy: 0 warning, 1 fail',
                                        `timeout` int(11) DEFAULT '0' COMMENT 'timeout length,unit: minute',
                                        `delay_time` int(11) DEFAULT '0' COMMENT 'delay execution time,unit: minute',
                                        `resource_ids` text COMMENT 'resource id, separated by comma',
                                        `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
                                        `task_group_priority` tinyint(4) DEFAULT '0' COMMENT 'task group priority',
                                        `create_time` datetime NOT NULL COMMENT 'create time',
                                        `update_time` datetime NOT NULL COMMENT 'update time',
                                        PRIMARY KEY (`id`),
                                        UNIQUE KEY `uniq_code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for t_ds_task_definition_log
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_ds_task_definition_log`;
CREATE TABLE `seatunnel`.`t_ds_task_definition_log` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
                                            `code` bigint(20) NOT NULL COMMENT 'encoding',
                                            `name` varchar(200) DEFAULT NULL COMMENT 'task definition name',
                                            `version` int(11) DEFAULT '0' COMMENT 'task definition version',
                                            `description` text COMMENT 'description',
                                            `project_code` bigint(20) NOT NULL COMMENT 'project code',
                                            `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
                                            `task_type` varchar(50) NOT NULL COMMENT 'task type',
                                            `task_params` longtext COMMENT 'job custom parameters',
                                            `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
                                            `task_priority` tinyint(4) DEFAULT '2' COMMENT 'job priority',
                                            `worker_group` varchar(200) DEFAULT NULL COMMENT 'worker grouping',
                                            `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                            `fail_retry_times` int(11) DEFAULT NULL COMMENT 'number of failed retries',
                                            `fail_retry_interval` int(11) DEFAULT NULL COMMENT 'failed retry interval',
                                            `timeout_flag` tinyint(2) DEFAULT '0' COMMENT 'timeout flag:0 close, 1 open',
                                            `timeout_notify_strategy` tinyint(4) DEFAULT NULL COMMENT 'timeout notification policy: 0 warning, 1 fail',
                                            `timeout` int(11) DEFAULT '0' COMMENT 'timeout length,unit: minute',
                                            `delay_time` int(11) DEFAULT '0' COMMENT 'delay execution time,unit: minute',
                                            `resource_ids` text DEFAULT NULL COMMENT 'resource id, separated by comma',
                                            `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
                                            `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
                                            `task_group_priority` tinyint(4) DEFAULT 0 COMMENT 'task group priority',
                                            `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
                                            `create_time` datetime NOT NULL COMMENT 'create time',
                                            `update_time` datetime NOT NULL COMMENT 'update time',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `uniq_code_version` (`code`,`version`) USING BTREE,
                                            KEY `idx_project_code` (`project_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE utf8_bin;



-- ----------------------------
-- Table structure for t_ds_process_task_relation
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_ds_process_task_relation`;
CREATE TABLE `seatunnel`.`t_ds_process_task_relation` (
                                              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
                                              `name` varchar(200) DEFAULT NULL COMMENT 'relation name',
                                              `project_code` bigint(20) NOT NULL COMMENT 'project code',
                                              `process_definition_code` bigint(20) NOT NULL COMMENT 'process code',
                                              `process_definition_version` int(11) NOT NULL COMMENT 'process version',
                                              `pre_task_code` bigint(20) NOT NULL COMMENT 'pre task code',
                                              `pre_task_version` int(11) NOT NULL COMMENT 'pre task version',
                                              `post_task_code` bigint(20) NOT NULL COMMENT 'post task code',
                                              `post_task_version` int(11) NOT NULL COMMENT 'post task version',
                                              `condition_type` tinyint(2) DEFAULT NULL COMMENT 'condition type : 0 none, 1 judge 2 delay',
                                              `condition_params` text COMMENT 'condition params(json)',
                                              `create_time` datetime NOT NULL COMMENT 'create time',
                                              `update_time` datetime NOT NULL COMMENT 'update time',
                                              PRIMARY KEY (`id`),
                                              KEY `idx_code` (`project_code`,`process_definition_code`),
                                              KEY `idx_pre_task_code_version` (`pre_task_code`,`pre_task_version`),
                                              KEY `idx_post_task_code_version` (`post_task_code`,`post_task_version`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- ----------------------------
-- Table structure for t_ds_process_task_relation_log
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_ds_process_task_relation_log`;
CREATE TABLE `seatunnel`.`t_ds_process_task_relation_log` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
                                                  `name` varchar(200) DEFAULT NULL COMMENT 'relation name',
                                                  `project_code` bigint(20) NOT NULL COMMENT 'project code',
                                                  `process_definition_code` bigint(20) NOT NULL COMMENT 'process code',
                                                  `process_definition_version` int(11) NOT NULL COMMENT 'process version',
                                                  `pre_task_code` bigint(20) NOT NULL COMMENT 'pre task code',
                                                  `pre_task_version` int(11) NOT NULL COMMENT 'pre task version',
                                                  `post_task_code` bigint(20) NOT NULL COMMENT 'post task code',
                                                  `post_task_version` int(11) NOT NULL COMMENT 'post task version',
                                                  `condition_type` tinyint(2) DEFAULT NULL COMMENT 'condition type : 0 none, 1 judge 2 delay',
                                                  `condition_params` text COMMENT 'condition params(json)',
                                                  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
                                                  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
                                                  `create_time` datetime NOT NULL COMMENT 'create time',
                                                  `update_time` datetime NOT NULL COMMENT 'update time',
                                                  PRIMARY KEY (`id`),
                                                  KEY `idx_process_code_version` (`process_definition_code`,`process_definition_version`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE utf8_bin;




-- ----------------------------
-- Table structure for t_st_job_definition
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_st_job_definition`;
CREATE TABLE IF NOT EXISTS `seatunnel`.`t_st_job_definition` (
    `id` bigint(20) NOT NULL,
    `name` varchar(50) NOT NULL,
    `description` varchar(255) ,
    `job_type` varchar(50),
    `create_user_id` int(11) NOT NULL,
    `update_user_id` int(11) NOT NULL,
    `project_code` bigint(20) NOT NULL,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    key job_definition_index (project_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 collate=utf8mb4_bin;


-- ----------------------------
-- Records of t_st_datasource
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_st_datasource`;
CREATE TABLE IF NOT EXISTS `seatunnel`.`t_st_datasource`
(
    id                bigint                      NOT NULL,
    datasource_name   varchar(63)                 NOT NULL,
    plugin_name       varchar(63)                 NOT NULL,
    plugin_version    varchar(63) default '1.0.0' NULL,
    datasource_config varchar(1023)               NOT NULL,
    description       varchar(63)                 NULL,
    create_user_id    int                         NOT NULL,
    update_user_id    int                         NOT NULL,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    unique key t_st_datasource_datasource_name_uindex (datasource_name),
    primary key (`id`)
    )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 collate=utf8mb4_bin;


-- ----------------------------
-- Table structure for t_st_job_task
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_st_job_task`;
CREATE TABLE IF NOT EXISTS `seatunnel`.`t_st_job_task` (
    `id` bigint(20) NOT NULL,
    `version_id` bigint(20) NOT NULL,
    `plugin_id` varchar(50) NOT NULL,
    `name` varchar(50) NOT NULL,
    `config` text,
    `transform_options` varchar(5000),
    `output_schema` text,
    `connector_type` varchar(50) NOT NULL,
    `datasource_id` bigint(20),
    `datasource_option` varchar(5000),
    `select_table_fields` varchar(5000),
    `scene_mode` varchar(50),
    `type` varchar(50) NOT NULL,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    key job_task_plugin_id_index (plugin_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 collate=utf8mb4_bin;


-- ----------------------------
-- Table structure for t_st_job_version
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_st_job_version`;
CREATE TABLE IF NOT EXISTS `seatunnel`.`t_st_job_version` (
    `id` bigint(20) NOT NULL,
    `job_id` bigint(20) NOT NULL,
    `name` varchar(255) NOT NULL,
    `job_mode` varchar(10) NOT NULL,
    `env` text,
    `engine_name` varchar(50) NOT NULL,
    `engine_version` varchar(50) NOT NULL,
    `create_user_id` int(11) NOT NULL,
    `update_user_id` int(11) NOT NULL,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 collate=utf8mb4_bin;

-- ----------------------------
-- Records of t_st_virtual_table
-- ----------------------------
DROP TABLE IF EXISTS `seatunnel`.`t_st_virtual_table`;
CREATE TABLE IF NOT EXISTS `seatunnel`.`t_st_virtual_table`
(
    id                    bigint        NOT NULL,
    datasource_id         bigint        NOT NULL,
    virtual_database_name varchar(63)   NOT NULL,
    virtual_table_name    varchar(63)   NOT NULL,
    table_fields          varchar(1023) NOT NULL,
    virtual_table_config  varchar(1023) NOT NULL,
    description           varchar(63)   NULL,
    create_user_id        int           NOT NULL,
    update_user_id        int           NOT NULL,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    primary key (`id`)
    )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 collate=utf8mb4_bin;
