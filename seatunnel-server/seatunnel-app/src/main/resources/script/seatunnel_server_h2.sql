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

-- CREATE DATABASE IF NOT EXISTS seatunnel;


-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role` CASCADE;;
CREATE TABLE `role` (
    `id` int(20) NOT NULL AUTO_INCREMENT,
    `type` int(2) NOT NULL,
    `role_name` varchar(255) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of role
-- ----------------------------

-- ----------------------------
-- Table structure for role_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `role_user_relation` CASCADE;
CREATE TABLE `role_user_relation` (
    `id` int(20) NOT NULL AUTO_INCREMENT,
    `role_id` int(20) NOT NULL,
    `user_id` int(20) NOT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of role_user_relation
-- ----------------------------

-- ----------------------------
-- Table structure for scheduler_config
-- ----------------------------
DROP TABLE IF EXISTS `scheduler_config` CASCADE;
CREATE TABLE `scheduler_config` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `script_id` int(11) DEFAULT NULL,
    `trigger_expression` varchar(255) DEFAULT NULL,
    `retry_times` int(11) NOT NULL DEFAULT '0',
    `retry_interval` int(11) NOT NULL DEFAULT '0',
    `active_start_time` datetime(3) NOT NULL,
    `active_end_time` datetime(3) NOT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `creator_id` int(11) NOT NULL,
    `update_id` int(11) NOT NULL,
    PRIMARY KEY (`id`)
);
-- ----------------------------
-- Records of scheduler_config
-- ----------------------------

-- ----------------------------
-- Table structure for script
-- ----------------------------
DROP TABLE IF EXISTS `script` CASCADE;
CREATE TABLE `script` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `type` tinyint(4) NOT NULL,
    `status` tinyint(4) NOT NULL,
    `content` mediumtext,
    `content_md5` varchar(255) DEFAULT NULL,
    `creator_id` int(11) NOT NULL,
    `mender_id` int(11) NOT NULL,
    `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of script
-- ----------------------------

-- ----------------------------
-- Table structure for script_job_apply
-- ----------------------------
DROP TABLE IF EXISTS `script_job_apply` CASCADE;
CREATE TABLE `script_job_apply` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `script_id` int(11) NOT NULL,
    `scheduler_config_id` int(11) NOT NULL,
    `job_id` bigint(20) DEFAULT NULL,
    `operator_id` int(11) NOT NULL,
    `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of script_job_apply
-- ----------------------------

-- ----------------------------
-- Table structure for script_param
-- ----------------------------
DROP TABLE IF EXISTS `script_param` CASCADE;
CREATE TABLE `script_param` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `script_id` int(11) DEFAULT NULL,
    `key` varchar(255) NOT NULL,
    `value` varchar(255) DEFAULT NULL,
    `status` tinyint(4) DEFAULT NULL,
    `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of script_param
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user` CASCADE;
CREATE TABLE `user` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `username` varchar(255) NOT NULL,
    `password` varchar(255) NOT NULL,
    `status` tinyint(4) NOT NULL,
    `type` tinyint(4) NOT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- Table structure for user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `user_login_log` CASCADE;
CREATE TABLE `user_login_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `token` mediumtext NOT NULL,
    `token_status` tinyint(1) NOT NULL,
    `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of user_login_log
-- ----------------------------

INSERT INTO `seatunnel`.`user`(`username`,`password`,`status`,`type`) values ('admin', '7f97da8846fed829bb8d1fd9f8030f3b', 0, 0);



-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_definition CASCADE;
CREATE TABLE t_ds_task_definition
(
    id                      int(11) NOT NULL AUTO_INCREMENT,
    code                    bigint(20) NOT NULL,
    name                    varchar(200) DEFAULT NULL,
    version                 int(11) DEFAULT NULL,
    description             text,
    user_id                 int(11) DEFAULT NULL,
    task_type               varchar(50) NOT NULL,
    task_params             longtext,
    flag                    tinyint(2) DEFAULT NULL,
    task_priority           tinyint(4) DEFAULT '2',
    worker_group            varchar(200) DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    fail_retry_times        int(11) DEFAULT NULL,
    fail_retry_interval     int(11) DEFAULT NULL,
    timeout_flag            tinyint(2) DEFAULT '0',
    timeout_notify_strategy tinyint(4) DEFAULT NULL,
    timeout                 int(11) DEFAULT '0',
    delay_time              int(11) DEFAULT '0',
    task_group_id           int(11) DEFAULT NULL,
    task_group_priority     tinyint(4) DEFAULT '0',
    resource_ids            text,
    create_time             datetime    NOT NULL,
    update_time             datetime     DEFAULT NULL,
    PRIMARY KEY (id, code)
);

-- ----------------------------
-- Table structure for t_ds_task_definition_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_definition_log CASCADE;
CREATE TABLE t_ds_task_definition_log
(
    id                      int(11) NOT NULL AUTO_INCREMENT,
    code                    bigint(20) NOT NULL,
    name                    varchar(200) DEFAULT NULL,
    version                 int(11) DEFAULT NULL,
    description             text,
    user_id                 int(11) DEFAULT NULL,
    task_type               varchar(50) NOT NULL,
    task_params             text,
    flag                    tinyint(2) DEFAULT NULL,
    task_priority           tinyint(4) DEFAULT '2',
    worker_group            varchar(200) DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    fail_retry_times        int(11) DEFAULT NULL,
    fail_retry_interval     int(11) DEFAULT NULL,
    timeout_flag            tinyint(2) DEFAULT '0',
    timeout_notify_strategy tinyint(4) DEFAULT NULL,
    timeout                 int(11) DEFAULT '0',
    delay_time              int(11) DEFAULT '0',
    resource_ids            text,
    operator                int(11) DEFAULT NULL,
    task_group_id           int(11) DEFAULT NULL,
    task_group_priority     tinyint(4) DEFAULT '0',
    operate_time            datetime     DEFAULT NULL,
    create_time             datetime    NOT NULL,
    update_time             datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);



-- ----------------------------
-- Table structure for t_ds_process_task_relation
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_task_relation CASCADE;
CREATE TABLE t_ds_process_task_relation
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(200) DEFAULT NULL,
    process_definition_version int(11) DEFAULT NULL,
    project_code               bigint(20) NOT NULL,
    process_definition_code    bigint(20) NOT NULL,
    pre_task_code              bigint(20) NOT NULL,
    pre_task_version           int(11) NOT NULL,
    post_task_code             bigint(20) NOT NULL,
    post_task_version          int(11) NOT NULL,
    condition_type             tinyint(2) DEFAULT NULL,
    condition_params           text,
    create_time                datetime NOT NULL,
    update_time                datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_process_task_relation_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_task_relation_log CASCADE;
CREATE TABLE t_ds_process_task_relation_log
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(200) DEFAULT NULL,
    process_definition_version int(11) DEFAULT NULL,
    project_code               bigint(20) NOT NULL,
    process_definition_code    bigint(20) NOT NULL,
    pre_task_code              bigint(20) NOT NULL,
    pre_task_version           int(11) NOT NULL,
    post_task_code             bigint(20) NOT NULL,
    post_task_version          int(11) NOT NULL,
    condition_type             tinyint(2) DEFAULT NULL,
    condition_params           text,
    operator                   int(11) DEFAULT NULL,
    operate_time               datetime     DEFAULT NULL,
    create_time                datetime NOT NULL,
    update_time                datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);





-- ----------------------------
-- Table structure for t_st_job_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_definition` CASCADE;
CREATE TABLE IF NOT EXISTS `t_st_job_definition` (
    `id` bigint(20) NOT NULL,
    `name` varchar(50) NOT NULL,
    `description` varchar(255) ,
    `job_type` varchar(50),
    `create_user_id` int(11) NOT NULL,
    `update_user_id` int(11) NOT NULL,
    `project_code` bigint(20) NOT NULL,
    `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
    ) ;


-- ----------------------------
-- Records of t_st_datasource
-- ----------------------------
DROP TABLE IF EXISTS `t_st_datasource` CASCADE;
CREATE TABLE IF NOT EXISTS `t_st_datasource`
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
    primary key (`id`)
    );


-- ----------------------------
-- Table structure for t_st_job_task
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_task` CASCADE;
CREATE TABLE IF NOT EXISTS `t_st_job_task` (
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
    PRIMARY KEY (`id`)
    );


-- ----------------------------
-- Table structure for t_st_job_version
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_version` CASCADE;
CREATE TABLE IF NOT EXISTS `t_st_job_version` (
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
    );

-- ----------------------------
-- Records of t_st_virtual_table
-- ----------------------------
DROP TABLE IF EXISTS `t_st_virtual_table` CASCADE;
CREATE TABLE IF NOT EXISTS `t_st_virtual_table`
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
    );
