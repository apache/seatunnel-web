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

CREATE DATABASE IF NOT EXISTS seatunnel;
use seatunnel;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `type` int(2) NOT NULL,
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `seatunnel`.`role`(`type`,`role_name`,`description`) values (0, 'ADMIN_ROLE', 'Admin User');
INSERT INTO `seatunnel`.`role`(`type`,`role_name`,`description`) values (1, 'NORMAL_ROLE', 'Normal User');

-- ----------------------------
-- Table structure for role_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `role_user_relation`;
CREATE TABLE `role_user_relation`  (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `role_id` int(20) NOT NULL,
  `user_id` int(20) NOT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_datasource
-- ----------------------------
DROP TABLE IF EXISTS `t_st_datasource`;
CREATE TABLE `t_st_datasource`  (
  `id` bigint(20) NOT NULL,
  `datasource_name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `plugin_name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `plugin_version` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '1.0.0',
  `datasource_config` varchar(1023) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_user_id` int(11) NOT NULL,
  `update_user_id` int(11) NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `t_st_datasource_datasource_name_uindex`(`datasource_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_definition`;
CREATE TABLE `t_st_job_definition`  (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `job_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_user_id` int(11) NOT NULL,
  `update_user_id` int(11) NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_instance`;
CREATE TABLE `t_st_job_instance`  (
  `id` bigint(20) NOT NULL,
  `job_define_id` bigint(20) NOT NULL,
  `job_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `job_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `engine_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `engine_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `job_engine_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_user_id` int(20) NOT NULL,
  `update_user_id` int(20) NULL DEFAULT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `end_time` timestamp(3) NULL DEFAULT NULL,
  `job_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_instance_history
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_instance_history`;
CREATE TABLE `t_st_job_instance_history`  (
  `id` bigint(20) NOT NULL,
  `dag` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_line
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_line`;
CREATE TABLE `t_st_job_line`  (
  `id` bigint(20) NOT NULL,
  `version_id` bigint(20) NOT NULL,
  `input_plugin_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `target_plugin_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `job_line_version_index`(`version_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_metrics
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_metrics`;
CREATE TABLE `t_st_job_metrics`  (
  `id` bigint(20) NOT NULL,
  `job_instance_id` bigint(20) NOT NULL,
  `pipeline_id` int(20) NOT NULL,
  `read_row_count` bigint(20) NOT NULL,
  `write_row_count` bigint(20) NOT NULL,
  `source_table_names` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `sink_table_names` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `read_qps` bigint(20) NULL DEFAULT NULL,
  `write_qps` bigint(20) NULL DEFAULT NULL,
  `record_delay` bigint(20) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_user_id` int(20) NOT NULL,
  `update_user_id` int(20) NULL DEFAULT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_task
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_task`;
CREATE TABLE `t_st_job_task`  (
  `id` bigint(20) NOT NULL,
  `version_id` bigint(20) NOT NULL,
  `plugin_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `transform_options` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `output_schema` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `connector_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `datasource_id` bigint(20) NULL DEFAULT NULL,
  `datasource_option` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `select_table_fields` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `scene_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `job_task_plugin_id_index`(`plugin_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_job_version
-- ----------------------------
DROP TABLE IF EXISTS `t_st_job_version`;
CREATE TABLE `t_st_job_version`  (
  `id` bigint(20) NOT NULL,
  `job_id` bigint(20) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `job_mode` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `env` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `engine_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `engine_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_user_id` int(11) NOT NULL,
  `update_user_id` int(11) NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_st_virtual_table
-- ----------------------------
DROP TABLE IF EXISTS `t_st_virtual_table`;
CREATE TABLE `t_st_virtual_table`  (
  `id` bigint(20) NOT NULL,
  `datasource_id` bigint(20) NOT NULL,
  `virtual_database_name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `virtual_table_name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `table_fields` varchar(1023) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `virtual_table_config` varchar(1023) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_user_id` int(11) NOT NULL,
  `update_user_id` int(11) NOT NULL,
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` tinyint(4) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `token` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `token_status` tinyint(1) NOT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_login_log
-- ----------------------------

INSERT INTO `seatunnel`.`user`(`username`,`password`,`status`,`type`) values ('admin', '7f97da8846fed829bb8d1fd9f8030f3b', 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
