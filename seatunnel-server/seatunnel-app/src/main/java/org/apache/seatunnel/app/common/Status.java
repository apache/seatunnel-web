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

package org.apache.seatunnel.app.common;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

/** status enum // todo #4855 One category one interval */
public enum Status {
    SUCCESS(0, "success", "成功"),
    FAILED(1, "failed", "失败"),
    INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),

    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    TASK_TIMEOUT_PARAMS_ERROR(10002, "task timeout parameter is not valid", "任务超时参数无效"),
    USER_NAME_EXIST(10003, "user name already exists", "用户名已存在"),
    USER_NAME_NULL(10004, "user name is null", "用户名不能为空"),
    HDFS_OPERATION_ERROR(10006, "hdfs operation error", "hdfs操作错误"),
    TASK_INSTANCE_NOT_FOUND(10008, "task instance not found", "任务实例不存在"),
    OS_TENANT_CODE_EXIST(10009, "os tenant code {0} already exists", "操作系统租户[{0}]已存在"),
    USER_NOT_EXIST(10010, "user {0} not exists", "用户[{0}]不存在"),
    ALERT_GROUP_NOT_EXIST(10011, "alarm group not found", "告警组不存在"),
    ALERT_GROUP_EXIST(10012, "alarm group already exists", "告警组名称已存在"),
    USER_NAME_PASSWD_ERROR(10013, "user name or password error", "用户名或密码错误"),
    LOGIN_SESSION_FAILED(10014, "create session failed!", "创建session失败"),
    DATASOURCE_EXIST(10015, "data source name already exists", "数据源名称已存在"),
    DATASOURCE_CONNECT_FAILED(10016, "data source connection failed", "建立数据源连接失败"),
    TENANT_NOT_EXIST(10017, "tenant not exists", "租户不存在"),
    PROJECT_NOT_FOUND(10018, "project {0} not found ", "项目[{0}]不存在"),
    PROJECT_ALREADY_EXISTS(10019, "project {0} already exists", "项目名称[{0}]已存在"),
    TASK_INSTANCE_NOT_EXISTS(10020, "task instance {0} does not exist", "任务实例[{0}]不存在"),
    TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE(
            10021, "task instance {0} is not sub process instance", "任务实例[{0}]不是子流程实例"),
    SCHEDULE_CRON_NOT_EXISTS(10022, "scheduler crontab {0} does not exist", "调度配置定时表达式[{0}]不存在"),
    SCHEDULE_CRON_ONLINE_FORBID_UPDATE(
            10023, "online status does not allow update operations", "调度配置上线状态不允许修改"),
    SCHEDULE_CRON_CHECK_FAILED(
            10024, "scheduler crontab expression validation failure: {0}", "调度配置定时表达式验证失败: {0}"),
    MASTER_NOT_EXISTS(10025, "master does not exist", "无可用master节点"),
    SCHEDULE_STATUS_UNKNOWN(10026, "unknown status: {0}", "未知状态: {0}"),
    CREATE_ALERT_GROUP_ERROR(10027, "create alert group error", "创建告警组错误"),
    QUERY_ALL_ALERTGROUP_ERROR(10028, "query all alertgroup error", "查询告警组错误"),
    LIST_PAGING_ALERT_GROUP_ERROR(10029, "list paging alert group error", "分页查询告警组错误"),
    UPDATE_ALERT_GROUP_ERROR(10030, "update alert group error", "更新告警组错误"),
    DELETE_ALERT_GROUP_ERROR(10031, "delete alert group error", "删除告警组错误"),
    ALERT_GROUP_GRANT_USER_ERROR(10032, "alert group grant user error", "告警组授权用户错误"),
    CREATE_DATASOURCE_ERROR(10033, "create datasource error", "创建数据源错误"),
    UPDATE_DATASOURCE_ERROR(10034, "update datasource error", "更新数据源错误"),
    QUERY_DATASOURCE_ERROR(10035, "query datasource error", "查询数据源错误"),
    CONNECT_DATASOURCE_FAILURE(10036, "connect datasource failure", "建立数据源连接失败"),
    CONNECTION_TEST_FAILURE(10037, "connection test failure", "测试数据源连接失败"),
    DELETE_DATA_SOURCE_FAILURE(10038, "delete data source failure", "删除数据源失败"),
    VERIFY_DATASOURCE_NAME_FAILURE(10039, "verify datasource name failure", "验证数据源名称失败"),
    UNAUTHORIZED_DATASOURCE(10040, "unauthorized datasource", "未经授权的数据源"),
    AUTHORIZED_DATA_SOURCE(10041, "authorized data source", "授权数据源失败"),
    LOGIN_SUCCESS(10042, "login success", "登录成功"),
    USER_LOGIN_FAILURE(10043, "user login failure", "用户登录失败"),
    LIST_WORKERS_ERROR(10044, "list workers error", "查询worker列表错误"),
    LIST_MASTERS_ERROR(10045, "list masters error", "查询master列表错误"),
    UPDATE_PROJECT_ERROR(10046, "update project error", "更新项目信息错误"),
    QUERY_PROJECT_DETAILS_BY_CODE_ERROR(10047, "query project details by code error", "查询项目详细信息错误"),
    CREATE_PROJECT_ERROR(10048, "create project error", "创建项目错误"),
    LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR(
            10049, "login user query project list paging error", "分页查询项目列表错误"),
    DELETE_PROJECT_ERROR(10050, "delete project error", "删除项目错误"),
    QUERY_UNAUTHORIZED_PROJECT_ERROR(10051, "query unauthorized project error", "查询未授权项目错误"),
    QUERY_AUTHORIZED_PROJECT(10052, "query authorized project", "查询授权项目错误"),
    QUERY_QUEUE_LIST_ERROR(10053, "query queue list error", "查询队列列表错误"),
    CREATE_RESOURCE_ERROR(10054, "create resource error", "创建资源错误"),
    UPDATE_RESOURCE_ERROR(10055, "update resource error", "更新资源错误"),
    QUERY_RESOURCES_LIST_ERROR(10056, "query resources list error", "查询资源列表错误"),
    QUERY_RESOURCES_LIST_PAGING(10057, "query resources list paging", "分页查询资源列表错误"),
    DELETE_RESOURCE_ERROR(10058, "delete resource error", "删除资源错误"),
    VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR(
            10059, "verify resource by name and type error", "资源名称或类型验证错误"),
    VIEW_RESOURCE_FILE_ON_LINE_ERROR(10060, "view resource file online error", "查看资源文件错误"),
    CREATE_RESOURCE_FILE_ON_LINE_ERROR(10061, "create resource file online error", "创建资源文件错误"),
    RESOURCE_FILE_CATNOT_BE_EMPTY(10062, "resource file is empty", "资源文件内容不能为空"),
    EDIT_RESOURCE_FILE_ON_LINE_ERROR(10063, "edit resource file online error", "更新资源文件错误"),
    DOWNLOAD_RESOURCE_FILE_ERROR(10064, "download resource file error", "下载资源文件错误"),
    CREATE_UDF_FUNCTION_ERROR(10065, "create udf function error", "创建UDF函数错误"),
    VIEW_UDF_FUNCTION_ERROR(10066, "view udf function error", "查询UDF函数错误"),
    UPDATE_UDF_FUNCTION_ERROR(10067, "update udf function error", "更新UDF函数错误"),
    QUERY_UDF_FUNCTION_LIST_PAGING_ERROR(
            10068, "query udf function list paging error", "分页查询UDF函数列表错误"),
    QUERY_DATASOURCE_BY_TYPE_ERROR(10069, "query datasource by type error", "查询数据源信息错误"),
    VERIFY_UDF_FUNCTION_NAME_ERROR(10070, "verify udf function name error", "UDF函数名称验证错误"),
    DELETE_UDF_FUNCTION_ERROR(10071, "delete udf function error", "删除UDF函数错误"),
    AUTHORIZED_FILE_RESOURCE_ERROR(10072, "authorized file resource error", "授权资源文件错误"),
    AUTHORIZE_RESOURCE_TREE(10073, "authorize resource tree display error", "授权资源目录树错误"),
    UNAUTHORIZED_UDF_FUNCTION_ERROR(10074, "unauthorized udf function error", "查询未授权UDF函数错误"),
    AUTHORIZED_UDF_FUNCTION_ERROR(10075, "authorized udf function error", "授权UDF函数错误"),
    CREATE_SCHEDULE_ERROR(10076, "create schedule error", "创建调度配置错误"),
    UPDATE_SCHEDULE_ERROR(10077, "update schedule error", "更新调度配置错误"),
    PUBLISH_SCHEDULE_ONLINE_ERROR(10078, "publish schedule online error", "上线调度配置错误"),
    OFFLINE_SCHEDULE_ERROR(10079, "offline schedule error", "下线调度配置错误"),
    QUERY_SCHEDULE_LIST_PAGING_ERROR(10080, "query schedule list paging error", "分页查询调度配置列表错误"),
    QUERY_SCHEDULE_LIST_ERROR(10081, "query schedule list error", "查询调度配置列表错误"),
    QUERY_TASK_LIST_PAGING_ERROR(10082, "query task list paging error", "分页查询任务列表错误"),
    QUERY_TASK_RECORD_LIST_PAGING_ERROR(10083, "query task record list paging error", "分页查询任务记录错误"),
    CREATE_TENANT_ERROR(10084, "create tenant error", "创建租户错误"),
    QUERY_TENANT_LIST_PAGING_ERROR(10085, "query tenant list paging error", "分页查询租户列表错误"),
    QUERY_TENANT_LIST_ERROR(10086, "query tenant list error", "查询租户列表错误"),
    UPDATE_TENANT_ERROR(10087, "update tenant error", "更新租户错误"),
    DELETE_TENANT_BY_ID_ERROR(10088, "delete tenant by id error", "删除租户错误"),
    VERIFY_OS_TENANT_CODE_ERROR(10089, "verify os tenant code error", "操作系统租户验证错误"),
    CREATE_USER_ERROR(10090, "create user error", "创建用户错误"),
    QUERY_USER_LIST_PAGING_ERROR(10091, "query user list paging error", "分页查询用户列表错误"),
    UPDATE_USER_ERROR(10092, "update user error", "更新用户错误"),
    DELETE_USER_BY_ID_ERROR(10093, "delete user by id error", "删除用户错误"),
    GRANT_PROJECT_ERROR(10094, "grant project error", "授权项目错误"),
    GRANT_RESOURCE_ERROR(10095, "grant resource error", "授权资源错误"),
    GRANT_UDF_FUNCTION_ERROR(10096, "grant udf function error", "授权UDF函数错误"),
    GRANT_DATASOURCE_ERROR(10097, "grant datasource error", "授权数据源错误"),
    GET_USER_INFO_ERROR(10098, "get user info error", "获取用户信息错误"),
    USER_LIST_ERROR(10099, "user list error", "查询用户列表错误"),
    VERIFY_USERNAME_ERROR(10100, "verify username error", "用户名验证错误"),
    UNAUTHORIZED_USER_ERROR(10101, "unauthorized user error", "查询未授权用户错误"),
    AUTHORIZED_USER_ERROR(10102, "authorized user error", "查询授权用户错误"),
    QUERY_TASK_INSTANCE_LOG_ERROR(10103, "view task instance log error", "查询任务实例日志错误"),
    DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR(
            10104, "download task instance log file error", "下载任务日志文件错误"),
    CREATE_PROCESS_DEFINITION_ERROR(10105, "create process definition error", "创建工作流错误"),
    VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR(
            10106, "verify process definition name unique error", "工作流定义名称验证错误"),
    UPDATE_PROCESS_DEFINITION_ERROR(10107, "update process definition error", "更新工作流定义错误"),
    RELEASE_PROCESS_DEFINITION_ERROR(10108, "release process definition error", "上线工作流错误"),
    QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR(
            10109, "query detail of process definition error", "查询工作流详细信息错误"),
    QUERY_PROCESS_DEFINITION_LIST(10110, "query process definition list", "查询工作流列表错误"),
    ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR(
            10111, "encapsulation treeview structure error", "查询工作流树形图数据错误"),
    GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR(
            10112, "get tasks list by process definition id error", "查询工作流定义节点信息错误"),
    QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR(
            10113, "query process instance list paging error", "分页查询工作流实例列表错误"),
    QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR(
            10114, "query task list by process instance id error", "查询任务实例列表错误"),
    UPDATE_PROCESS_INSTANCE_ERROR(10115, "update process instance error", "更新工作流实例错误"),
    QUERY_PROCESS_INSTANCE_BY_ID_ERROR(10116, "query process instance by id error", "查询工作流实例错误"),
    DELETE_PROCESS_INSTANCE_BY_ID_ERROR(10117, "delete process instance by id error", "删除工作流实例错误"),
    QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR(
            10118, "query sub process instance detail info by task id error", "查询子流程任务实例错误"),
    QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR(
            10119,
            "query parent process instance detail info by sub process instance id error",
            "查询子流程该工作流实例错误"),
    QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR(
            10120, "query process instance all variables error", "查询工作流自定义变量信息错误"),
    ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR(
            10121, "encapsulation process instance gantt structure error", "查询工作流实例甘特图数据错误"),
    QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR(
            10122, "query process definition list paging error", "分页查询工作流定义列表错误"),
    SIGN_OUT_ERROR(10123, "sign out error", "退出错误"),
    OS_TENANT_CODE_HAS_ALREADY_EXISTS(10124, "os tenant code has already exists", "操作系统租户已存在"),
    IP_IS_EMPTY(10125, "ip is empty", "IP地址不能为空"),
    SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE(
            10126, "schedule release is already {0}", "调度配置上线错误[{0}]"),
    CREATE_QUEUE_ERROR(10127, "create queue error", "创建队列错误"),
    QUEUE_NOT_EXIST(10128, "queue {0} not exists", "队列ID[{0}]不存在"),
    QUEUE_VALUE_EXIST(10129, "queue value {0} already exists", "队列值[{0}]已存在"),
    QUEUE_NAME_EXIST(10130, "queue name {0} already exists", "队列名称[{0}]已存在"),
    UPDATE_QUEUE_ERROR(10131, "update queue error", "更新队列信息错误"),
    NEED_NOT_UPDATE_QUEUE(10132, "no content changes, no updates are required", "数据未变更，不需要更新队列信息"),
    VERIFY_QUEUE_ERROR(10133, "verify queue error", "验证队列信息错误"),
    NAME_NULL(10134, "name must be not null", "名称不能为空"),
    NAME_EXIST(10135, "name {0} already exists", "名称[{0}]已存在"),
    SAVE_ERROR(10136, "save error", "保存错误"),
    DELETE_PROJECT_ERROR_DEFINES_NOT_NULL(
            10137, "please delete the process definitions in project first!", "请先删除全部工作流定义"),
    BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR(
            10117, "batch delete process instance by ids {0} error", "批量删除工作流实例错误: {0}"),
    PREVIEW_SCHEDULE_ERROR(10139, "preview schedule error", "预览调度配置错误"),
    PARSE_TO_CRON_EXPRESSION_ERROR(10140, "parse cron to cron expression error", "解析调度表达式错误"),
    SCHEDULE_START_TIME_END_TIME_SAME(
            10141, "The start time must not be the same as the end", "开始时间不能和结束时间一样"),
    DELETE_TENANT_BY_ID_FAIL(
            10142,
            "delete tenant by id fail, for there are {0} process instances in executing using it",
            "删除租户失败，有[{0}]个运行中的工作流实例正在使用"),
    DELETE_TENANT_BY_ID_FAIL_DEFINES(
            10143,
            "delete tenant by id fail, for there are {0} process definitions using it",
            "删除租户失败，有[{0}]个工作流定义正在使用"),
    DELETE_TENANT_BY_ID_FAIL_USERS(
            10144,
            "delete tenant by id fail, for there are {0} users using it",
            "删除租户失败，有[{0}]个用户正在使用"),
    DELETE_WORKER_GROUP_BY_ID_FAIL(
            10145,
            "delete worker group by id fail, for there are {0} process instances in executing using it",
            "删除Worker分组失败，有[{0}]个运行中的工作流实例正在使用"),
    QUERY_WORKER_GROUP_FAIL(10146, "query worker group fail ", "查询worker分组失败"),
    DELETE_WORKER_GROUP_FAIL(10147, "delete worker group fail ", "删除worker分组失败"),
    USER_DISABLED(10148, "The current user is disabled", "当前用户已停用"),
    COPY_PROCESS_DEFINITION_ERROR(
            10149, "copy process definition from {0} to {1} error : {2}", "从{0}复制工作流到{1}错误 : {2}"),
    MOVE_PROCESS_DEFINITION_ERROR(
            10150, "move process definition from {0} to {1} error : {2}", "从{0}移动工作流到{1}错误 : {2}"),
    SWITCH_PROCESS_DEFINITION_VERSION_ERROR(
            10151, "Switch process definition version error", "切换工作流版本出错"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR(
            10152,
            "Switch process definition version error: not exists process definition, [process definition id {0}]",
            "切换工作流版本出错：工作流不存在，[工作流id {0}]"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR(
            10153,
            "Switch process definition version error: not exists process definition version, [process definition id {0}] [version number {1}]",
            "切换工作流版本出错：工作流版本信息不存在，[工作流id {0}] [版本号 {1}]"),
    QUERY_PROCESS_DEFINITION_VERSIONS_ERROR(
            10154, "query process definition versions error", "查询工作流历史版本信息出错"),
    DELETE_PROCESS_DEFINITION_VERSION_ERROR(
            10156, "delete process definition version error", "删除工作流历史版本出错"),

    QUERY_USER_CREATED_PROJECT_ERROR(
            10157, "query user created project error error", "查询用户创建的项目错误"),
    PROCESS_DEFINITION_CODES_IS_EMPTY(10158, "process definition codes is empty", "工作流CODES不能为空"),
    BATCH_COPY_PROCESS_DEFINITION_ERROR(10159, "batch copy process definition error", "复制工作流错误"),
    BATCH_MOVE_PROCESS_DEFINITION_ERROR(10160, "batch move process definition error", "移动工作流错误"),
    QUERY_WORKFLOW_LINEAGE_ERROR(10161, "query workflow lineage error", "查询血缘失败"),

    QUERY_WORKFLOW_LEVEL_IN_LINEAGE_ERROR(
            1016101, "query workflow level in lineage error", "查询血缘中的工作流层级失败"),

    QUERY_WORKFLOW_LINEAGE_ERROR_NOT_EXIST_PROCESS_INSTANCE(
            1016102,
            "query workflow lineage error, not exist process instance [{0}]",
            "查询血缘失败，不存在工作流实例[{0}]"),
    QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR(
            10162, "query authorized and user created project error error", "查询授权的和用户创建的项目错误"),
    DELETE_PROCESS_DEFINITION_EXECUTING_FAIL(
            10163,
            "delete process definition [{0}] fail, for there are [{1}] process instances in executing using it",
            "删除工作流定义[{0}]失败，有[{1}]个运行中的工作流实例正在使用"),
    CHECK_OS_TENANT_CODE_ERROR(
            10164,
            "Tenant code invalid, should follow linux's users naming conventions",
            "非法的租户名，需要遵守 Linux 用户命名规范"),
    FORCE_TASK_SUCCESS_ERROR(10165, "force task success error", "强制成功任务实例错误"),
    TASK_INSTANCE_STATE_OPERATION_ERROR(
            10166,
            "the status of task instance {0} is {1},Cannot perform force success operation",
            "任务实例[{0}]的状态是[{1}]，无法执行强制成功操作"),
    DATASOURCE_TYPE_NOT_EXIST(10167, "data source type not exist", "数据源类型不存在"),
    PROCESS_DEFINITION_NAME_EXIST(
            10168, "process definition name {0} already exists", "工作流定义名称[{0}]已存在"),
    DATASOURCE_DB_TYPE_ILLEGAL(10169, "datasource type illegal", "数据源类型参数不合法"),
    DATASOURCE_PORT_ILLEGAL(10170, "datasource port illegal", "数据源端口参数不合法"),
    DATASOURCE_OTHER_PARAMS_ILLEGAL(10171, "datasource other params illegal", "数据源其他参数不合法"),
    DATASOURCE_NAME_ILLEGAL(10172, "datasource name illegal", "数据源名称不合法"),
    DATASOURCE_HOST_ILLEGAL(10173, "datasource host illegal", "数据源HOST不合法"),
    DELETE_WORKER_GROUP_NOT_EXIST(10174, "delete worker group not exist ", "删除worker分组不存在"),
    CREATE_WORKER_GROUP_FORBIDDEN_IN_DOCKER(
            10175, "create worker group forbidden in docker ", "创建worker分组在docker中禁止"),
    DELETE_WORKER_GROUP_FORBIDDEN_IN_DOCKER(
            10176, "delete worker group forbidden in docker ", "删除worker分组在docker中禁止"),
    WORKER_ADDRESS_INVALID(10177, "worker address {0} invalid", "worker地址[{0}]无效"),
    QUERY_WORKER_ADDRESS_LIST_FAIL(10178, "query worker address list fail ", "查询worker地址列表失败"),
    TRANSFORM_PROJECT_OWNERSHIP(
            10179, "Please transform project ownership [{0}]", "请先转移项目所有权[{0}]"),
    QUERY_ALERT_GROUP_ERROR(10180, "query alert group error", "查询告警组错误"),
    CURRENT_LOGIN_USER_TENANT_NOT_EXIST(
            10181, "the tenant of the currently login user is not specified", "未指定当前登录用户的租户"),
    REVOKE_PROJECT_ERROR(10182, "revoke project error", "撤销项目授权错误"),
    QUERY_AUTHORIZED_USER(10183, "query authorized user error", "查询拥有项目权限的用户错误"),

    DATASOURCE_CONNECT_REJECT_KEYWORD(
            10184,
            "data source connection does not allow the [{0}] keyword",
            "数据源连接参数不允许使用[{0}]关键字"),
    PROJECT_NOT_EXIST(10190, "This project was not found. Please refresh page.", "该项目不存在,请刷新页面"),
    TASK_INSTANCE_HOST_IS_NULL(10191, "task instance host is null", "任务实例host为空"),
    QUERY_EXECUTING_WORKFLOW_ERROR(10192, "query executing workflow error", "查询运行的工作流实例错误"),
    DELETE_PROCESS_DEFINITION_USE_BY_OTHER_FAIL(
            10193,
            "delete process definition [{0}] fail, cause used by other tasks: {1}",
            "删除工作流[{0}]失败，被其他任务引用：{1}"),
    DELETE_TASK_USE_BY_OTHER_FAIL(
            10194,
            "delete task {0} fail, cause used by other tasks: {1}",
            "删除任务 {0} 失败，被其他任务引用：{1}"),
    TASK_WITH_DEPENDENT_ERROR(10195, "task used in other tasks", "删除被其他任务引用"),
    DELETE_PROCESS_DEFINITION_SCHEDULE_CRON_ERROR(
            10196, "delete workflow [{0}] schedule error", "删除工作流 [{0}] 调度配置错误"),
    DELETE_PROCESS_DEFINITION_SUCCESS(10197, "delete workflow [{0}] success", "删除工作流 [{0}] 成功"),
    LIST_TASK_TYPE_ERROR(10200, "list task type error", "查询任务类型列表错误"),
    DELETE_TASK_TYPE_ERROR(10200, "delete task type error", "删除任务类型错误"),
    ADD_TASK_TYPE_ERROR(10200, "add task type error", "添加任务类型错误"),

    LIST_ALERT_SERVERS_ERROR(10198, "list AlertServer error", "查询AlertServer列表错误"),

    LIST_API_SERVERS_ERROR(10199, "list ApiServer error", "查询ApiServer列表错误"),

    LISTING_EXECUTING_WORKFLOWS_BY_MASTER_ERROR(
            10200, "listing executing workflows by master address error", "查询Master中运行的工作流实例信息错误"),
    LISTING_DISPATCHING_TASK_INSTANCES_BY_MASTER_ERROR(
            10201,
            "listing dispatching TaskInstance by master address error",
            "查询Master中正在分发的任务实例信息错误"),
    LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR(
            10202,
            "listing executing TaskExecutionContext by worker address error",
            "查询Worker中正在执行的任务上下文信息错误"),
    QUERY_TASK_EXECUTION_CONTEXT_DETAIL_ERROR(
            10203, "query task execution context detail error", "查询Worker中的task上下文详情错误"),
    CLEAN_TASK_INSTANCE_ERROR(10204, "clean task instance state error", "清除任务实例状态错误"),
    CLEAN_TASK_INSTANCE_ERROR_WORKFLOW_INSTANCE_IS_RUNNING(
            10205,
            "clean task instance state error, the related workflow instance is running",
            "清除任务实例状态错误，对应的工作流实例在运行中"),

    CLEAN_TASK_INSTANCE_ERROR_COMMAND_ALREADY_EXIST(
            10206,
            "clean task instance state error, the related workflow instance is already exist a state clean operation, please try later",
            "清除任务实例状态错误，对应的工作流实例已经有一个清除状态的操作，请稍后再试"),
    QUERY_EXECUTING_WORKFLOWS_DETAIL_BY_ID(
            10207, "query executing workflows detail by id error", "查询Master中运行的工作流实例详情错误"),

    CLEAN_TASK_INSTANCE_ERROR_CURRENT_TASK_INSTANCE_IS_IN_ISOLATION_CHAIN(
            10208,
            "clean task instance state error, the current task instance is in coronation chain, please cancel the isolation first",
            "清除任务实例状态错误，当前的任务实例在隔离链，请先解除隔离"),

    CLEAN_TASK_INSTANCE_ERROR_CURRENT_TASK_INSTANCE_IS_IN_PAUSE(
            10209,
            "clean task instance state error, the current task instance is in coronation chain or isolation pause, no need to clean, it will be restored automatically when the condition is met",
            "清除任务实例状态错误，当前的任务实例在加冕或隔离暂停中，无需清除重跑，条件允许后将自动恢复"),

    PROJECT_CODE_NOT_EXIST(
            10210, "This project {0} was not found. Please refresh page.", "项目{0}不存在,请刷新页面"),
    CLEAN_TASK_INSTANCE_ERROR_UPSTREAM_WORKFLOW_INSTANCE_IS_RUNNING(
            10211,
            "clean task instance state error, the related upstream workflow instance [{0}]is running",
            "清除任务实例状态错误，对应工作流实例为子工作流，但其引用该工作流的上游工作流[{0}]正在运行中"),

    DEPENDENT_RUN_ERROR(10212, "dependent run error", "依赖链运行操作失败"),

    CREATE_BATCH_RESOURCE_NOTES(10204, "create batch resource error", "创建资源错误"),
    QUERY_PROJECT_DETAILS_BY_NAME_ERROR(10205, "query project details by name error", "查询项目详细信息错误"),
    UDF_FUNCTION_NOT_EXIST(20001, "UDF function not found", "UDF函数不存在"),
    UDF_FUNCTION_EXISTS(20002, "UDF function already exists", "UDF函数已存在"),
    RESOURCE_NOT_EXIST(20004, "resource not exist", "资源不存在"),
    RESOURCE_EXIST(20005, "resource already exists", "资源已存在"),
    RESOURCE_SUFFIX_NOT_SUPPORT_VIEW(
            20006, "resource suffix do not support online viewing", "资源文件后缀不支持查看"),
    RESOURCE_SIZE_EXCEED_LIMIT(20007, "upload resource file size exceeds limit", "上传资源文件大小超过限制"),
    RESOURCE_SUFFIX_FORBID_CHANGE(
            20008, "resource suffix not allowed to be modified", "资源文件后缀不支持修改"),
    UDF_RESOURCE_SUFFIX_NOT_JAR(
            20009, "UDF resource suffix name must be jar", "UDF资源文件后缀名只支持[jar]"),
    HDFS_COPY_FAIL(20010, "hdfs copy {0} -> {1} fail", "hdfs复制失败：[{0}] -> [{1}]"),
    RESOURCE_FILE_EXIST(
            20011,
            "resource file {0} already exists in hdfs,please delete it or change name!",
            "资源文件[{0}]在hdfs中已存在，请删除或修改资源名"),
    RESOURCE_FILE_NOT_EXIST(20012, "resource file {0} not exists !", "资源文件[{0}]不存在"),
    UDF_RESOURCE_IS_BOUND(
            20013, "udf resource file is bound by UDF functions:{0}", "udf函数绑定了资源文件[{0}]"),
    RESOURCE_IS_USED(20014, "resource file is used by process definition", "资源文件被上线的流程定义使用了"),
    PARENT_RESOURCE_NOT_EXIST(20015, "parent resource not exist", "父资源文件不存在"),
    RESOURCE_NOT_EXIST_OR_NO_PERMISSION(
            20016,
            "resource not exist or no permission,please view the task node and remove error resource",
            "请检查任务节点并移除无权限或者已删除的资源"),
    RESOURCE_IS_AUTHORIZED(
            20017,
            "resource is authorized to user {0},suffix not allowed to be modified",
            "资源文件已授权其他用户[{0}],后缀不允许修改"),
    RESOURCE_HAS_FOLDER(
            20018, "There are files or folders in the current directory:{0}", "当前目录下有文件或文件夹[{0}]"),

    BATCH_RESOURCE_NAME_REPEAT(20019, "duplicate file names in this batch", "此批处理中存在重复的文件名"),
    RESOURCE_FILE_IS_EMPTY(20020, "resource file [{0}] is empty", "资源文件 [{0}] 内容不能为空"),

    RESOURCE_OWNER_OR_TENANT_CHANGE_ERROR(
            20021,
            "resource[{0}] owner is deleted or the tenant is unbound, the operation is not allowed",
            "资源[{0}]所属用户被删除或者其租户被解除绑定, 无法进行该操作"),

    RESOURCE_CREATE_ERROR(20022, "resource[{0}] create error", "资源[{0}]创建失败"),

    UPLOAD_FOLDER_WARNING(
            20023,
            "upload folder warning, create resource failed[{0}], delete resource failed[{1}], other resources upload success",
            "文件夹上传警告，以下资源创建失败[{0}], 以下资源覆盖删除失败[{1}], 其余均已上传成功"),

    UPLOAD_FOLDER_CREATE_WARNING(
            20024,
            "upload folder warning, create resource failed[{0}], other resources upload success",
            "文件夹上传警告，以下资源创建失败[{0}], 其余均已上传成功"),

    UPLOAD_FOLDER_DELETE_WARNING(
            20025,
            "upload folder warning, delete resource failed[{0}], other resources upload success",
            "文件夹上传警告, 以下资源覆盖删除失败[{0}], 其余均已上传成功"),

    RESOURCE_LIST_IS_USED(
            20026, "resource file is used by process definition, [{0}]", "资源文件被上线的流程定义使用了, [{0}]"),

    RESOURCE_IS_USED_BY_PROCESS(
            20027, "resource file is used by process definition", "资源文件被流程定义使用了"),

    USER_NO_OPERATION_PERM(30001, "user has no operation privilege", "当前用户没有操作权限"),
    USER_NO_OPERATION_PROJECT_PERM(
            30002,
            "User [{0}] does not have this operation permission under the [{1}] project",
            "用户[{0}]在[{1}]项目下无此操作权限"),

    USER_CAN_NOT_OPERATION(
            30003, "User [{0}] does not have permission for Project [{1}]", "用户[{0}]在项目[{1}]下无此权限"),

    PROCESS_INSTANCE_NOT_EXIST(50001, "process instance {0} does not exist", "工作流实例[{0}]不存在"),
    PROCESS_INSTANCE_EXIST(50002, "process instance {0} already exists", "工作流实例[{0}]已存在"),
    PROCESS_DEFINE_NOT_EXIST(50003, "process definition [{0}] does not exist", "工作流定义[{0}]不存在"),
    PROCESS_DEFINE_NOT_RELEASE(
            50004,
            "process definition {0} process version {1} not on line",
            "工作流定义[{0}] 工作流版本[{1}]不是上线状态"),
    SUB_PROCESS_DEFINE_NOT_RELEASE(
            50004, "exist sub process definition not on line", "存在子工作流定义不是上线状态"),
    PROCESS_INSTANCE_ALREADY_CHANGED(
            50005, "the status of process instance {0} is already {1}", "工作流实例[{0}]的状态已经是[{1}]"),
    PROCESS_INSTANCE_STATE_OPERATION_ERROR(
            50006,
            "the status of process instance {0} is {1},Cannot perform {2} operation",
            "工作流实例[{0}]的状态是[{1}]，无法执行[{2}]操作"),
    SUB_PROCESS_INSTANCE_NOT_EXIST(
            50007, "the task belong to process instance does not exist", "子工作流实例不存在"),
    PROCESS_DEFINE_NOT_ALLOWED_EDIT(
            50008, "process definition {0} does not allow edit", "工作流定义[{0}]不允许修改"),
    PROCESS_INSTANCE_EXECUTING_COMMAND(
            50009,
            "process instance {0} is executing the command, please wait ...",
            "工作流实例[{0}]正在执行命令，请稍等..."),
    PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE(
            50010, "process instance {0} is not sub process instance", "工作流实例[{0}]不是子工作流实例"),
    TASK_INSTANCE_STATE_COUNT_ERROR(50011, "task instance state count error", "查询各状态任务实例数错误"),
    COUNT_PROCESS_INSTANCE_STATE_ERROR(50012, "count process instance state error", "查询各状态流程实例数错误"),
    COUNT_PROCESS_DEFINITION_USER_ERROR(
            50013, "count process definition user error", "查询各用户流程定义数错误"),
    START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),
    BATCH_START_PROCESS_INSTANCE_ERROR(
            50014, "batch start process instance error: {0}", "批量运行工作流实例错误: {0}"),
    PROCESS_INSTANCE_ERROR(50014, "process instance delete error: {0}", "工作流实例删除[{0}]错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
    CHECK_PROCESS_DEFINITION_ERROR(50016, "check process definition error", "工作流定义错误"),
    QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR(
            50017, "query recipients and copyers by process definition error", "查询收件人和抄送人错误"),
    DATA_IS_NOT_VALID(50017, "data {0} not valid", "数据[{0}]无效"),
    DATA_IS_NULL(50018, "data {0} is null", "数据[{0}]不能为空"),
    PROCESS_NODE_HAS_CYCLE(50019, "process node has cycle", "流程节点间存在循环依赖"),
    PROCESS_NODE_S_PARAMETER_INVALID(50020, "process node {0} parameter invalid", "流程节点[{0}]参数无效"),
    PROCESS_DEFINE_STATE_ONLINE(
            50021, "process definition [{0}] is already on line", "工作流定义[{0}]已上线"),
    DELETE_PROCESS_DEFINE_BY_CODE_ERROR(
            50022, "delete process definition by code error", "删除工作流定义错误"),
    SCHEDULE_CRON_STATE_ONLINE(
            50023, "the status of schedule {0} is already on line", "调度配置[{0}]已上线"),
    DELETE_SCHEDULE_CRON_BY_ID_ERROR(50024, "delete schedule by id error", "删除调度配置错误"),
    BATCH_DELETE_PROCESS_DEFINE_ERROR(
            50025, "batch delete process definition error", "批量删除工作流定义错误"),
    BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR(
            50026, "batch delete process definition by codes error {0}", "批量删除工作流定义错误 [{0}]"),
    DELETE_PROCESS_DEFINE_BY_CODES_ERROR(
            50026, "delete process definition by codes {0} error", "删除工作流定义[{0}]错误"),
    TENANT_NOT_SUITABLE(
            50027,
            "there is not any tenant suitable, please choose a tenant available.",
            "没有合适的租户，请选择可用的租户"),
    EXPORT_PROCESS_DEFINE_BY_ID_ERROR(50028, "export process definition by id error", "导出工作流定义错误"),
    BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR(
            50028, "batch export process definition by ids error", "批量导出工作流定义错误"),
    IMPORT_PROCESS_DEFINE_ERROR(50029, "import process definition error", "导入工作流定义错误"),
    TASK_DEFINE_NOT_EXIST(50030, "task definition [{0}] does not exist", "任务定义[{0}]不存在"),
    CREATE_PROCESS_TASK_RELATION_ERROR(50032, "create process task relation error", "创建工作流任务关系错误"),
    PROCESS_TASK_RELATION_NOT_EXIST(
            50033, "process task relation [{0}] does not exist", "工作流任务关系[{0}]不存在"),
    PROCESS_TASK_RELATION_EXIST(
            50034,
            "process task relation is already exist, processCode:[{0}]",
            "工作流任务关系已存在, processCode:[{0}]"),
    PROCESS_DAG_IS_EMPTY(50035, "process dag is empty", "工作流dag是空"),
    CHECK_PROCESS_TASK_RELATION_ERROR(50036, "check process task relation error", "工作流任务关系参数错误"),
    CREATE_TASK_DEFINITION_ERROR(50037, "create task definition error", "创建任务错误"),
    UPDATE_TASK_DEFINITION_ERROR(50038, "update task definition error", "更新任务定义错误"),
    QUERY_TASK_DEFINITION_VERSIONS_ERROR(
            50039, "query task definition versions error", "查询任务历史版本信息出错"),
    SWITCH_TASK_DEFINITION_VERSION_ERROR(50040, "Switch task definition version error", "切换任务版本出错"),
    DELETE_TASK_DEFINITION_VERSION_ERROR(
            50041, "delete task definition version error", "删除任务历史版本出错"),
    DELETE_TASK_DEFINE_BY_CODE_ERROR(50042, "delete task definition by code error", "删除任务定义错误"),
    QUERY_DETAIL_OF_TASK_DEFINITION_ERROR(
            50043, "query detail of task definition error", "查询任务详细信息错误"),
    QUERY_TASK_DEFINITION_LIST_PAGING_ERROR(
            50044, "query task definition list paging error", "分页查询任务定义列表错误"),
    TASK_DEFINITION_NAME_EXISTED(
            50045, "task definition name [{0}] already exists", "任务定义名称[{0}]已经存在"),
    RELEASE_TASK_DEFINITION_ERROR(50046, "release task definition error", "上线任务错误"),
    MOVE_PROCESS_TASK_RELATION_ERROR(50047, "move process task relation error", "移动任务到其他工作流错误"),
    DELETE_TASK_PROCESS_RELATION_ERROR(50048, "delete process task relation error", "删除工作流任务关系错误"),
    QUERY_TASK_PROCESS_RELATION_ERROR(50049, "query process task relation error", "查询工作流任务关系错误"),
    TASK_DEFINE_STATE_ONLINE(50050, "task definition [{0}] is already on line", "任务定义[{0}]已上线"),
    TASK_HAS_DOWNSTREAM(50051, "Task exists downstream [{0}] dependence", "任务存在下游[{0}]依赖"),
    TASK_HAS_UPSTREAM(50052, "Task [{0}] exists upstream dependence", "任务[{0}]存在上游依赖"),
    MAIN_TABLE_USING_VERSION(50053, "the version that the master table is using", "主表正在使用该版本"),
    PROJECT_PROCESS_NOT_MATCH(50054, "the project and the process is not match", "项目和工作流不匹配"),
    DELETE_EDGE_ERROR(50055, "delete edge error", "删除工作流任务连接线错误"),
    NOT_SUPPORT_UPDATE_TASK_DEFINITION(
            50056, "task state does not support modification", "当前任务不支持修改"),
    NOT_SUPPORT_COPY_TASK_TYPE(50057, "task type [{0}] does not support copy", "不支持复制的任务类型[{0}]"),
    BATCH_EXECUTE_PROCESS_INSTANCE_ERROR(
            50058, "change process instance status error: {0}", "修改工作实例状态错误: {0}"),
    PROCESS_DEFINE_RELEASE(
            50059,
            "process definition {0} process version {1} on line",
            "工作流定义[{0}] 工作流版本[{1}]是上线状态"),
    TASK_HAVE_EMPTY_LOCAL_PARAM(50060, "task {0} have empty local parameter", "任务[{0}]存在为空的本地参数"),
    HDFS_NOT_STARTUP(60001, "hdfs not startup", "hdfs未启用"),
    STORAGE_NOT_STARTUP(60002, "storage not startup", "存储未启用"),
    S3_CANNOT_RENAME(60003, "directory cannot be renamed", "S3无法重命名文件夹"),

    /** for monitor */
    QUERY_DATABASE_STATE_ERROR(70001, "query database state error", "查询数据库状态错误"),

    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(70011, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(
            70012, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(70013, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(70014, "delete access token error", "删除访问token错误"),
    ACCESS_TOKEN_NOT_EXIST(70015, "access token not exist", "访问token不存在"),
    QUERY_ACCESSTOKEN_BY_USER_ERROR(70016, "query access token by user error", "查询访问指定用户的token错误"),

    COMMAND_STATE_COUNT_ERROR(80001, "task instance state count error", "查询各状态任务实例数错误"),
    NEGTIVE_SIZE_NUMBER_ERROR(80002, "query size number error", "查询size错误"),
    START_TIME_BIGGER_THAN_END_TIME_ERROR(
            80003, "start time bigger than end time error", "开始时间在结束时间之后错误"),
    QUEUE_COUNT_ERROR(90001, "queue count error", "查询队列数据错误"),

    KERBEROS_STARTUP_STATE(100001, "get kerberos startup state error", "获取kerberos启动状态错误"),

    // audit log
    QUERY_AUDIT_LOG_LIST_PAGING(10057, "query resources list paging", "分页查询资源列表错误"),

    // plugin
    PLUGIN_NOT_A_UI_COMPONENT(
            110001, "query plugin error, this plugin has no UI component", "查询插件错误，此插件无UI组件"),
    QUERY_PLUGINS_RESULT_IS_NULL(
            110002,
            "query alarm plugins result is empty, please check the startup status of the alarm component and confirm that the relevant alarm plugin is successfully registered",
            "查询告警插件为空, 请检查告警组件启动状态并确认相关告警插件已注册成功"),
    QUERY_PLUGINS_ERROR(110003, "query plugins error", "查询插件错误"),
    QUERY_PLUGIN_DETAIL_RESULT_IS_NULL(110004, "query plugin detail result is null", "查询插件详情结果为空"),

    UPDATE_ALERT_PLUGIN_INSTANCE_ERROR(
            110005, "update alert plugin instance error", "更新告警组和告警组插件实例错误"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR(
            110006, "delete alert plugin instance error", "删除告警组和告警组插件实例错误"),
    GET_ALERT_PLUGIN_INSTANCE_ERROR(110007, "get alert plugin instance error", "获取告警组和告警组插件实例错误"),
    CREATE_ALERT_PLUGIN_INSTANCE_ERROR(
            110008, "create alert plugin instance error", "创建告警组和告警组插件实例错误"),
    QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR(
            110009, "query all alert plugin instance error", "查询所有告警实例失败"),
    PLUGIN_INSTANCE_ALREADY_EXIT(110010, "plugin instance already exit", "该告警插件实例已存在"),
    LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR(
            110011, "query plugin instance page error", "分页查询告警实例失败"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED(
            110012,
            "failed to delete the alert instance, there is an alarm group associated with this alert instance",
            "删除告警实例失败，存在与此告警实例关联的警报组"),
    PROCESS_DEFINITION_VERSION_IS_USED(
            110013, "this process definition version is used", "此工作流定义版本被使用"),

    CREATE_ENVIRONMENT_ERROR(120001, "create environment error", "创建环境失败"),
    ENVIRONMENT_NAME_EXISTS(120002, "this environment name [{0}] already exists", "环境名称[{0}]已经存在"),
    ENVIRONMENT_NAME_IS_NULL(120003, "this environment name shouldn't be empty.", "环境名称不能为空"),
    ENVIRONMENT_CONFIG_IS_NULL(120004, "this environment config shouldn't be empty.", "环境配置信息不能为空"),
    UPDATE_ENVIRONMENT_ERROR(120005, "update environment [{0}] info error", "更新环境[{0}]信息失败"),
    DELETE_ENVIRONMENT_ERROR(120006, "delete environment error", "删除环境信息失败"),
    DELETE_ENVIRONMENT_RELATED_TASK_EXISTS(
            120007,
            "this environment has been used in tasks,so you can't delete it.",
            "该环境已经被任务使用，所以不能删除该环境信息"),
    QUERY_ENVIRONMENT_BY_NAME_ERROR(1200008, "not found environment [{0}] ", "查询环境名称[{0}]信息不存在"),
    QUERY_ENVIRONMENT_BY_CODE_ERROR(1200009, "not found environment [{0}] ", "查询环境编码[{0}]不存在"),
    QUERY_ENVIRONMENT_ERROR(1200010, "login user query environment error", "分页查询环境列表错误"),
    VERIFY_ENVIRONMENT_ERROR(1200011, "verify environment error", "验证环境信息错误"),
    GET_RULE_FORM_CREATE_JSON_ERROR(
            1200012, "get rule form create json error", "获取规则 FROM-CREATE-JSON 错误"),
    QUERY_RULE_LIST_PAGING_ERROR(1200013, "query rule list paging error", "获取规则分页列表错误"),
    QUERY_RULE_LIST_ERROR(1200014, "query rule list error", "获取规则列表错误"),
    QUERY_RULE_INPUT_ENTRY_LIST_ERROR(1200015, "query rule list error", "获取规则列表错误"),
    QUERY_EXECUTE_RESULT_LIST_PAGING_ERROR(
            1200016, "query execute result list paging error", "获取数据质量任务结果分页错误"),
    GET_DATASOURCE_OPTIONS_ERROR(1200017, "get datasource options error", "获取数据源Options错误"),
    GET_DATASOURCE_TABLES_ERROR(1200018, "get datasource tables error", "获取数据源表列表错误"),
    GET_DATASOURCE_TABLE_COLUMNS_ERROR(1200019, "get datasource table columns error", "获取数据源表列名错误"),

    CREATE_CLUSTER_ERROR(120020, "create cluster error", "创建集群失败"),
    CLUSTER_NAME_EXISTS(120021, "this cluster name [{0}] already exists", "集群名称[{0}]已经存在"),
    CLUSTER_NAME_IS_NULL(120022, "this cluster name shouldn't be empty.", "集群名称不能为空"),
    CLUSTER_CONFIG_IS_NULL(120023, "this cluster config shouldn't be empty.", "集群配置信息不能为空"),
    UPDATE_CLUSTER_ERROR(120024, "update cluster [{0}] info error", "更新集群[{0}]信息失败"),
    DELETE_CLUSTER_ERROR(120025, "delete cluster error", "删除集群信息失败"),
    DELETE_CLUSTER_RELATED_TASK_EXISTS(
            120026,
            "this cluster has been used in tasks,so you can't delete it.",
            "该集群已经被任务使用，所以不能删除该集群信息"),
    QUERY_CLUSTER_BY_NAME_ERROR(1200027, "not found cluster [{0}] ", "查询集群名称[{0}]信息不存在"),
    QUERY_CLUSTER_BY_CODE_ERROR(1200028, "not found cluster [{0}] ", "查询集群编码[{0}]不存在"),
    QUERY_CLUSTER_ERROR(1200029, "login user query cluster error", "分页查询集群列表错误"),
    VERIFY_CLUSTER_ERROR(1200030, "verify cluster error", "验证集群信息错误"),
    CLUSTER_PROCESS_DEFINITIONS_IS_INVALID(
            1200031, "cluster worker groups is invalid format", "集群关联的工作组参数解析错误"),
    UPDATE_CLUSTER_PROCESS_DEFINITION_RELATION_ERROR(
            1200032,
            "You can't modify the process definition, because the process definition [{0}] and this cluster [{1}] already be used in the task [{2}]",
            "您不能修改集群选项，因为该工作流组 [{0}] 和 该集群 [{1}] 已经被用在任务 [{2}] 中"),
    CLUSTER_NOT_EXISTS(120033, "this cluster can not found in db.", "集群配置数据库里查询不到为空"),
    DELETE_CLUSTER_RELATED_NAMESPACE_EXISTS(
            120034,
            "this cluster has been used in namespace,so you can't delete it.",
            "该集群已经被命名空间使用，所以不能删除该集群信息"),

    TASK_GROUP_NAME_EXSIT(
            130001, "this task group name is repeated in a project", "该任务组名称在一个项目中已经使用"),
    TASK_GROUP_SIZE_ERROR(130002, "task group size error", "任务组大小应该为大于1的整数"),
    TASK_GROUP_STATUS_ERROR(130003, "task group status error", "任务组已经被关闭"),
    TASK_GROUP_FULL(130004, "task group is full", "任务组已经满了"),
    TASK_GROUP_USED_SIZE_ERROR(
            130005, "the used size number of task group is dirty", "任务组使用的容量发生了变化"),
    TASK_GROUP_QUEUE_RELEASE_ERROR(130006, "failed to release task group queue", "任务组资源释放时出现了错误"),
    TASK_GROUP_QUEUE_AWAKE_ERROR(130007, "awake waiting task failed", "任务组使唤醒等待任务时发生了错误"),
    CREATE_TASK_GROUP_ERROR(130008, "create task group error", "创建任务组错误"),
    UPDATE_TASK_GROUP_ERROR(130009, "update task group list error", "更新任务组错误"),
    QUERY_TASK_GROUP_LIST_ERROR(130010, "query task group list error", "查询任务组列表错误"),
    CLOSE_TASK_GROUP_ERROR(130011, "close task group error", "关闭任务组错误"),
    START_TASK_GROUP_ERROR(130012, "start task group error", "启动任务组错误"),
    QUERY_TASK_GROUP_QUEUE_LIST_ERROR(130013, "query task group queue list error", "查询任务组队列列表错误"),
    TASK_GROUP_CACHE_START_FAILED(130014, "cache start failed", "任务组相关的缓存启动失败"),
    ENVIRONMENT_WORKER_GROUPS_IS_INVALID(
            130015, "environment worker groups is invalid format", "环境关联的工作组参数解析错误"),
    UPDATE_ENVIRONMENT_WORKER_GROUP_RELATION_ERROR(
            130016,
            "You can't modify the worker group, because the worker group [{0}] and this environment [{1}] already be used in the task [{2}]",
            "您不能修改工作组选项，因为该工作组 [{0}] 和 该环境 [{1}] 已经被用在任务 [{2}] 中"),
    TASK_GROUP_QUEUE_ALREADY_START(130017, "task group queue already start", "节点已经获取任务组资源"),
    TASK_GROUP_STATUS_CLOSED(130018, "The task group has been closed.", "任务组已经被关闭"),
    TASK_GROUP_STATUS_OPENED(130019, "The task group has been opened.", "任务组已经被开启"),
    NOT_ALLOW_TO_DISABLE_OWN_ACCOUNT(130020, "Not allow to disable your own account", "不能停用自己的账号"),
    NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP(
            130030, "Not allow to delete the default alarm group ", "不能删除默认告警组"),
    TIME_ZONE_ILLEGAL(130031, "time zone [{0}] is illegal", "时区参数 [{0}] 不合法"),

    TASK_GROUP_USED_SIZE_NOT_EMPTY(
            130032, "the used size number of task group is not empty", "任务组已使用容量不为空"),

    QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR(
            1300001, "login user query k8s namespace list paging error", "分页查询k8s名称空间列表错误"),
    K8S_NAMESPACE_EXIST(1300002, "k8s namespace {0} already exists", "k8s命名空间[{0}]已存在"),
    CREATE_K8S_NAMESPACE_ERROR(1300003, "create k8s namespace error", "创建k8s命名空间错误"),
    UPDATE_K8S_NAMESPACE_ERROR(1300004, "update k8s namespace error", "更新k8s命名空间信息错误"),
    K8S_NAMESPACE_NOT_EXIST(1300005, "k8s namespace {0} not exists", "命名空间ID[{0}]不存在"),
    K8S_CLIENT_OPS_ERROR(1300006, "k8s error with exception {0}", "k8s操作报错[{0}]"),
    VERIFY_K8S_NAMESPACE_ERROR(1300007, "verify k8s and namespace error", "验证k8s命名空间信息错误"),
    DELETE_K8S_NAMESPACE_BY_ID_ERROR(1300008, "delete k8s namespace by id error", "删除命名空间错误"),
    VERIFY_PARAMETER_NAME_FAILED(1300009, "The file name [{0}] verify failed", "文件命名[{0}]校验失败"),
    STORE_OPERATE_CREATE_ERROR(1300010, "create the resource failed", "存储操作失败"),
    GRANT_K8S_NAMESPACE_ERROR(1300011, "grant namespace error", "授权资源错误"),
    QUERY_UNAUTHORIZED_NAMESPACE_ERROR(
            1300012, "query unauthorized namespace error", "查询未授权命名空间错误"),
    QUERY_AUTHORIZED_NAMESPACE_ERROR(1300013, "query authorized namespace error", "查询授权命名空间错误"),
    QUERY_CAN_USE_K8S_CLUSTER_ERROR(
            1300014, "login user query can used k8s cluster list error", "查询可用k8s集群错误"),
    RESOURCE_FULL_NAME_TOO_LONG_ERROR(1300015, "resource's fullname is too long error", "资源文件名过长"),
    TENANT_FULL_NAME_TOO_LONG_ERROR(1300016, "tenant's fullname is too long error", "租户名过长"),
    USER_PASSWORD_LENGTH_ERROR(1300017, "user's password length error", "用户密码长度错误"),
    QUERY_CAN_USE_K8S_NAMESPACE_ERROR(
            1300018, "login user query can used namespace list error", "查询可用命名空间错误"),
    FILE_NAME_CONTAIN_RESTRICTIONS(
            1300019, "The file name [{0}] contain restrictions", "文件命名[{0}]包含限制内容"),
    FILE_TYPE_IS_RESTRICTIONS(1300020, "The file [{0}] type is restrictions", "文件[{0}]类型为限制类型"),

    NO_CURRENT_OPERATING_PERMISSION(
            1400001, "The current user does not have this permission.", "当前用户无此权限"),
    FUNCTION_DISABLED(1400002, "The current feature is disabled.", "当前功能已被禁用"),
    SCHEDULE_TIME_NUMBER(1400003, "The number of complement dates exceed 100.", "补数日期个数超过100"),
    DESCRIPTION_TOO_LONG_ERROR(1400004, "description is too long error", "描述过长"),
    COMPLEMENT_COMMAND_SCHEDULE_TIME_EMPTY(
            1400005, "complement command schedule time can not be empty", "补数数据日期不可为空"),
    START_TIME_CAN_NOT_AFTER_END_TIME(1400006, "start time can not after end time", "开始时间不可晚于结束时间"),
    CONFIG_FILTER_EMPTY(
            1400007,
            "complement time is empty after filtering according to the configuration",
            "当前补数时间根据配置过滤后为空"),
    PROJECT_NAME_TOO_LONG_ERROR(1400008, "project name is too long error", "项目名称过长"),

    NO_CURRENT_OPERATING_PERMISSION_FOR_RESOURCE(
            1400009, "[{0}] is exist, current user no [{0}] permission", "[{0}]已存在，当前用户无[{0}]权限"),

    ISOLATION_TASK_NOT_EXIST(1500000, "Isolation task not exist", "隔离任务不存在"),

    ISOLATION_TASK_USER_NO_PERMISSION(
            1500001,
            "Current login user does not have isolation view permission",
            "当前用户不具备当前项目隔离列表查看权限"),
    ISOLATION_TASK_SUBMIT_ERROR(1500100, "Submit isolation task error", "提交隔离任务异常"),
    ISOLATION_TASK_SUBMIT_ERROR_WORKFLOW_INSTANCE_NOT_SUPPORT(
            1500101,
            "Submit isolation task error, relate workflow instance [{0}] is not support",
            "提交隔离任务异常, 关联的工作流实例：[{0}]暂不支持该操作"),
    ISOLATION_TASK_SUBMIT_ERROR_TASK_NOT_EXIST(
            1500103,
            "Submit isolation task error, task: [{0}] is not exist",
            "提交隔离任务异常, 任务不存在：[{0}]"),
    ISOLATION_TASK_SUBMIT_ERROR_EXIST_SUB_ISOLATION_TASK(
            1500104,
            "Submit isolation task error, workflow instance: [{0}] exist an sub isolation task",
            "提交隔离任务异常, 工作流实例已经存在：[{0}]子隔离任务"),
    ISOLATION_TASK_SUBMIT_ERROR_SEND_REQUEST_TO_MASTER_ERROR(
            1500105,
            "Submit isolation task error, send request to master error",
            "提交隔离任务异常，发送请求给Master异常"),

    ISOLATION_TASK_SUBMIT_USER_NO_PERMISSION(
            1500106,
            "Current login user does not have isolation submit permission for project {0}",
            "当前用户不具备{0}项目隔离的提交权限"),

    ISOLATION_TASK_ONLINE_ERROR(1500200, "Online isolation task error", "上线隔离任务异常"),
    ISOLATION_TASK_ONLINE_ERROR_ALREADY_ONLINE(
            1500201,
            "Online isolation task error, the isolation task is already online",
            "上线隔离任务异常，该任务已处于隔离中"),
    ISOLATION_TASK_ONLINE_ERROR_SEND_REQUEST_TO_MASTER_ERROR(
            1500202,
            "Online isolation task error, send request to master error",
            "上线隔离任务异常，发送隔离请求给Master异常"),
    ISOLATION_TASK_CANCEL_ERROR(1500300, "Cancel isolation task error", "取消隔离任务异常"),
    ISOLATION_TASK_CANCEL_ERROR_SEND_REQUEST_TO_MASTER_ERROR(
            1500301,
            "Cancel isolation task error, send request to master error",
            "取消隔离任务异常，发送隔离请求给Master异常"),
    ISOLATION_TASK_CANCEL_ERROR_THE_ISOLATION_ALREADY_CANCEL(
            1500302,
            "Cancel isolation task error, this task isolation is already been cancel",
            "取消隔离任务异常，该隔离已经下线"),
    ISOLATION_TASK_CANCEL_USER_NO_PERMISSION(
            1500304,
            "Current login user does not have isolation cancel permission",
            "当前用户不具备当前项目隔离取消权限"),
    ISOLATION_TASK_LISTING_ERROR(1500400, "Listing isolation task error", "查询隔离任务列表异常"),
    ISOLATION_TASK_DELETE_ERROR(1500500, "Delete isolation task error", "删除隔离任务异常"),
    ISOLATION_TASK_DELETE_ERROR_IS_NOT_OFFLINE(
            1500501,
            "Delete isolation task error, the task status is not offline",
            "删除隔离任务异常，该隔离任务尚未下线"),

    CORONATION_TASK_PARSE_ERROR(1600000, "Coronation task parse error", "解析加冕任务异常"),
    CORONATION_TASK_NOT_EXIST(1600001, "Coronation task not exist", "加冕任务不存在"),
    CORONATION_TASK_PARSE_ERROR_TASK_NODE_NAME_IS_NOT_VALIDATED(
            1600002,
            "Coronation task parse error, taskNode name is not validated",
            "解析加冕任务异常， 任务名不正确"),

    CORONATION_TASK_PARSE_ERROR_TASK_NODE_ALREADY_EXIST(
            1600003,
            "Coronation task parse error, workflowInstance: [{0}] taskNode:[{1}] is already in coronation",
            "解析加冕任务异常， 工作流实例: [{0}] 任务: [{1}] 已经被加冕"),

    CORONATION_TASK_USER_NO_PERMISSION(
            1600004,
            "Current login user does not have coronation view permission",
            "当前用户不具备当前项目加冕列表查看权限"),

    CORONATION_TASK_SUBMIT_ERROR(1600100, "Coronation task submit error", "提交加冕任务异常"),
    CORONATION_TASK_SUBMIT_USER_NO_PERMISSION(
            1600101,
            "Current login user does not have coronation submit permission",
            "当前用户不具备当前项目加冕提交权限"),
    CORONATION_TASK_ONLINE_ERROR(1600200, "Coronation task online error", "上线加冕任务异常"),
    CORONATION_TASK_CANCEL_ERROR(1600300, "Coronation task cancel error", "取消加冕任务异常"),
    CORONATION_TASK_CANCEL_USER_NO_PERMISSION(
            1500301,
            "Current login user does not have coronation cancel permission",
            "当前用户不具备当前项目加冕取消权限"),
    CORONATION_TASK_LISTING_ERROR(1600400, "Coronation task listing error", "查询加冕任务异常"),

    OPERATION_ALREADY_EXIST(1800000, "operation already exist", "操作已存在"),

    DATA_SOURCE_HAD_USED(
            1600000,
            "data source already used (projectName - workflowName - taskName):{0}",
            "数据源正在被使用(项目名 - 工作流名 - 任务名):{0}"),

    COMPLEMENT_CALENDAR_ERROR(1700000, "complement calendar error", "补数日历错误"),

    COMPLEMENT_MISSING_CALENDAR_PARAM(
            1700001, "complement missing calendar param [{0}]", "补数缺少日历参数 [{0}]"),

    COMPLEMENT_QUERY_DATE_LIST_ERROR(1700002, "complement query date list error", "补数查询日期列表错误"),
    ;

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    Status(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE
                .getLanguage()
                .equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }

    /** Retrieve Status enum entity by status code. */
    public static Optional<Status> findStatusBy(int code) {
        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
