"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""

from ambari_commons.constants import AMBARI_SUDO_BINARY
from resource_management.libraries.functions import StackFeature
from resource_management.libraries.functions import conf_select
from resource_management.libraries.functions import get_kinit_path
from resource_management.libraries.functions import stack_select
from resource_management.libraries.functions.default import default
from resource_management.libraries.functions.format import format
from resource_management.libraries.functions.get_not_managed_resources import get_not_managed_resources
from resource_management.libraries.functions.is_empty import is_empty
from resource_management.libraries.functions.setup_ranger_plugin_xml import get_audit_configs, \
  generate_ranger_service_config
from resource_management.libraries.functions.stack_features import check_stack_feature
from resource_management.libraries.functions.stack_features import get_stack_feature_version
from resource_management.libraries.resources.hdfs_resource import HdfsResource
from resource_management.libraries.script.script import Script

# shared configs
sudo = AMBARI_SUDO_BINARY
config = Script.get_config()
stack_root = Script.get_stack_root()
java64_home = config['ambariLevelParams']['java_home']
# get the correct version to use for checking stack features
version_for_stack_feature_checks = get_stack_feature_version(config)
stack_supports_ranger_kerberos = check_stack_feature(StackFeature.RANGER_KERBEROS_SUPPORT, version_for_stack_feature_checks)
stack_supports_ranger_audit_db = check_stack_feature(StackFeature.RANGER_AUDIT_DB_SUPPORT, version_for_stack_feature_checks)

security_enabled = config['configurations']['cluster-env']['security_enabled']
hdfs_user = config['configurations']['hadoop-env']['hdfs_user']
retryAble = default("/commandParams/command_retry_enabled", False)
version = default("/commandParams/version", None)


hadoop_bin_dir = stack_select.get_hadoop_dir("bin")
hadoop_conf_dir = conf_select.get_hadoop_conf_dir()
kinit_path_local = get_kinit_path(default('/configurations/kerberos-env/executable_search_paths', None))
# ToDo - check how to run seatunnel kerberos via mpack configurations instead
kinit_cmd = ""
kinit_cmd_master = ""
master_security_config = ""
hbase_decommission_auth_config = ""

#for create_hdfs_directory
hostname = config['agentLevelParams']['hostname']
hdfs_user_keytab = config['configurations']['hadoop-env']['hdfs_user_keytab']
hdfs_user = config['configurations']['hadoop-env']['hdfs_user']
hdfs_principal_name = config['configurations']['hadoop-env']['hdfs_principal_name']

hdfs_site = config['configurations']['hdfs-site']
default_fs = config['configurations']['core-site']['fs.defaultFS']

dfs_type = default("/clusterLevelParams/dfs_type", "")

import functools
#create partial functions with common arguments for every HdfsResource call
#to create/delete hdfs directory/file/copyfromlocal we need to call params.HdfsResource in code
HdfsResource = functools.partial(
  HdfsResource,
  user=hdfs_user,
  hdfs_resource_ignore_file = "/var/lib/ambari-agent/data/.hdfs_resource_ignore",
  security_enabled = security_enabled,
  keytab = hdfs_user_keytab,
  kinit_path_local = kinit_path_local,
  hadoop_bin_dir = hadoop_bin_dir,
  hadoop_conf_dir = hadoop_conf_dir,
  principal_name = hdfs_principal_name,
  hdfs_site = hdfs_site,
  default_fs = default_fs,
  immutable_paths = get_not_managed_resources(),
  dfs_type = dfs_type
)

#####################################
# Seatunnel configs
#####################################

seatunnel_home = stack_root + "/current/seatunnel"
seatunnel_conf_dir = '/etc/seatunnel/conf'
seatunnel_web_home = stack_root + "/current/seatunnel-web"
seatunnel_web_conf_dir = '/etc/seatunnel-web/conf'
seatunnel_lib = seatunnel_web_home + '/libs'
seatunnel_log_dir = '/var/log/seatunnel'
seatunnel_web_log_dir = "/var/log/seatunnel-web"
seatunnel_web_profile_dir = seatunnel_web_home+ "/profile"
ranger_seatunnel_plugin_home = seatunnel_web_home + '/ranger-seatunnel-plugin'

cred_lib_path = ranger_seatunnel_plugin_home+ "/install/lib/*"
cred_setup_prefix = (
    'ambari-python-wrap',
    f"{ranger_seatunnel_plugin_home}/ranger_credential_helper.py",
    '-l',
    f"{cred_lib_path}"
)

seatunnel_user = "root"
seatunnel_group = "root"

# Seatunnel config file paths
seatunnel_yaml = seatunnel_conf_dir + '/seatunnel.yaml'
seatunnel_hazelcast = seatunnel_conf_dir + '/hazelcast.yaml'
seatunnel_hazelcast_client = seatunnel_conf_dir + '/hazelcast-client.yaml'
seatunnel_env_file = seatunnel_conf_dir + '/seatunnel-env.sh'
seatunnel_home_config_file = '/etc/profile.d/seatunnel.sh'

seatunnel_jvm_options = seatunnel_conf_dir + '/jvm_options'
seatunnel_jvm_client_options = seatunnel_conf_dir + '/jvm_client_options'
seatunnel_log4j2 = seatunnel_conf_dir + '/log4j2.properties'
seatunnel_log4j2_client = seatunnel_conf_dir + '/log4j2_client.properties'

# Seatunnel-web config file paths
seatunnel_web_application = seatunnel_web_conf_dir + '/application.yml'  # NOTE: Currently, the file extension is .yml instead of .yaml
seatunnel_web_hazelcast_client = seatunnel_web_conf_dir + '/hazelcast-client.yaml'
seatunnel_web_logback = seatunnel_web_conf_dir + '/logback-spring.xml'
seatunnel_web_env_file = seatunnel_web_conf_dir + '/seatunnel-web-env.sh'

# Seatunnel and Seatunnel-web common configurations
seatunnel_cluster_name = config['configurations']['seatunnel-common']['seatunnel_cluster_name']
hazelcast_port = default('configurations/seatunnel-common/hazelcast_port', '5801')
hazelcast_client_content_template = config['configurations']['seatunnel-common']['hazelcast_client_config_content']


# Read content for seatunnel.yaml
backup_count = config['configurations']['seatunnel']["backup_count"]
seatunnel_content_template = config['configurations']['seatunnel']['seatunnel_config_content']
jvm_options_template = config['configurations']['seatunnel']['jvm_options']
jvm_client_options_template = config['configurations']['seatunnel']['jvm_client_options']
log4j2_properties_template = config['configurations']['seatunnel']['log4j2_properties']
log4j2_client_properties_template = config['configurations']['seatunnel']['log4j2_client_properties']
seatunnel_env_template = config['configurations']['seatunnel']['seatunnel_env']



# Read content for hazelcast.yaml
enable_rest_api = default('configurations/seatunnel-hazelcast/enable_rest_api', False)
hazelcast_content_template = config['configurations']['seatunnel-hazelcast']['hazelcast_config_content']

seatunnel_hosts_list = config['clusterHostInfo']['seatunnel_hosts']
seatunnel_hosts_list.sort()
hazelcast_cluster_members = "\n".join(
    [f"          - {host}:{hazelcast_port}" if i != 0 else f"- {host}:{hazelcast_port}" for i, host in
     enumerate(seatunnel_hosts_list)])

hazelcast_client_cluster_members = "\n".join(
    [f"      - {host}:{hazelcast_port}" if i != 0 else f"- {host}:{hazelcast_port}" for i, host in
     enumerate(seatunnel_hosts_list)])
# Read content for seatunnel-web-application.yml
seatunnel_web_hosts_list = config['clusterHostInfo']['seatunnel-web_hosts']
seatunnel_web_hosts_list.sort()
web_server_port = config['configurations']['seatunnel-web-application']['http_port']
seatunnel_web_ssl_enabled = config['configurations']['seatunnel-web-application']['seatunnel.web.ssl.enabled']
if (seatunnel_web_ssl_enabled):
  web_server_port = config['configurations']['seatunnel-web-application']['https_port']
  seatunnel_web_ssl_keystore = config['configurations']['seatunnel-web-application']['seatunnel.web.ssl.keystore']
  seatunnel_web_ssl_keystore_password = config['configurations']['seatunnel-web-application']['seatunnel.web.ssl.keystore.password']
  seatunnel_web_ssl_keystore_type = config['configurations']['seatunnel-web-application']['seatunnel.web.ssl.keystore.type']
  seatunnel_web_ssl_keystore_key_alias = config['configurations']['seatunnel-web-application']['seatunnel.web.ssl.keystore.key.alias']

db_driver_class = config['configurations']['seatunnel-web-application']['db_driver_class']
db_url = config['configurations']['seatunnel-web-application']['db_url']
db_username = config['configurations']['seatunnel-web-application']['db_username']
db_password = config['configurations']['seatunnel-web-application']['db_password']


write_job_definition_to_file = config['configurations']['seatunnel-web-application']['write_job_definition_to_file']
datasource_encryption_type = config['configurations']['seatunnel-web-application']['datasource_encryption_type']
jwt_expire_time = config['configurations']['seatunnel-web-application']['jwt_expire_time']
jwt_secret_key = config['configurations']['seatunnel-web-application']['jwt_secret_key']
jwt_algorithm = config['configurations']['seatunnel-web-application']['jwt_algorithm']

seatunnel_web_config_content = config['configurations']['seatunnel-web-application']['seatunnel_web_config_content']
seatunnel_web_log_config_content = config['configurations']['seatunnel-web-application']['seatunnel_web_log_config_content']
seatunnel_web_env_content = config['configurations']['seatunnel-web-application']['seatunnel_web_env']



# ranger hive plugin section start

# ranger host
ranger_admin_hosts = default("/clusterHostInfo/ranger_admin_hosts", [])
has_ranger_admin = not len(ranger_admin_hosts) == 0

# ranger support xml_configuration flag, instead of depending on ranger xml_configurations_supported/ranger-env, using stack feature
xml_configurations_supported = check_stack_feature(StackFeature.RANGER_XML_CONFIGURATION, version_for_stack_feature_checks)

# ranger seatunnel plugin enabled property
enable_ranger_seatunnel = default("/configurations/ranger-seatunnel-plugin-properties/ranger-seatunnel-plugin-enabled", "No")
enable_ranger_seatunnel = True if enable_ranger_seatunnel.lower() == 'yes' else False

access_controller_class = "org.apache.seatunnel.app.permission.SeatunnelAccessControllerDefaultImpl"
# get ranger seatunnel properties if enable_ranger_seatunnel is True
if enable_ranger_seatunnel:
  access_controller_class = "org.apache.ranger.authorization.seatunnel.authorizer.RangerSeatunnelAuthorizer"
  # get ranger policy url
  policymgr_mgr_url = config['configurations']['admin-properties']['policymgr_external_url']
  if xml_configurations_supported:
    policymgr_mgr_url = config['configurations']['ranger-seatunnel-security']['ranger.plugin.seatunnel.policy.rest.url']

  if not is_empty(policymgr_mgr_url) and policymgr_mgr_url.endswith('/'):
    policymgr_mgr_url = policymgr_mgr_url.rstrip('/')

  # ranger audit db user
  xa_audit_db_user = default('/configurations/admin-properties/audit_db_user', 'rangerlogger')

  # ranger seatunnel service name
  repo_name = str(config['clusterName']) + '_seatunnel'
  repo_name_value = config['configurations']['ranger-seatunnel-security']['ranger.plugin.seatunnel.service.name']
  if not is_empty(repo_name_value) and repo_name_value != "{{repo_name}}":
    repo_name = repo_name_value

  jdbc_driver_class_name = config['configurations']['ranger-seatunnel-plugin-properties']['jdbc.driverClassName']
  common_name_for_certificate = config['configurations']['ranger-seatunnel-plugin-properties']['common.name.for.certificate']
  repo_config_username = config['configurations']['ranger-seatunnel-plugin-properties']['REPOSITORY_CONFIG_USERNAME']

  # ranger-env config
  ranger_env = config['configurations']['ranger-env']

  # create ranger-env config having external ranger credential properties
  if not has_ranger_admin and enable_ranger_seatunnel:
    external_admin_username = default('/configurations/ranger-seatunnel-plugin-properties/external_admin_username', 'admin')
    external_admin_password = default('/configurations/ranger-seatunnel-plugin-properties/external_admin_password', 'admin')
    external_ranger_admin_username = default('/configurations/ranger-seatunnel-plugin-properties/external_ranger_admin_username', 'amb_ranger_admin')
    external_ranger_admin_password = default('/configurations/ranger-seatunnel-plugin-properties/external_ranger_admin_password', 'amb_ranger_admin')
    ranger_env = {}
    ranger_env['admin_username'] = external_admin_username
    ranger_env['admin_password'] = external_admin_password
    ranger_env['ranger_admin_username'] = external_ranger_admin_username
    ranger_env['ranger_admin_password'] = external_ranger_admin_password

  ranger_plugin_properties = config['configurations']['ranger-seatunnel-plugin-properties']
  policy_user = config['configurations']['ranger-seatunnel-plugin-properties']['policy_user']
  repo_config_password = config['configurations']['ranger-seatunnel-plugin-properties']['REPOSITORY_CONFIG_PASSWORD']

  ranger_downloaded_custom_connector = None
  ranger_previous_jdbc_jar_name = None
  ranger_driver_curl_source = None
  ranger_driver_curl_target = None
  ranger_previous_jdbc_jar = None

  if has_ranger_admin and stack_supports_ranger_audit_db:
  # to get db connector related properties
    xa_audit_db_flavor = config['configurations']['admin-properties']['DB_FLAVOR']
    ranger_jdbc_jar_name, ranger_previous_jdbc_jar_name, audit_jdbc_url, jdbc_driver = get_audit_configs(config)

    ranger_downloaded_custom_connector = format("{tmp_dir}/{ranger_jdbc_jar_name}")
    ranger_driver_curl_source = format("{jdk_location}/{ranger_jdbc_jar_name}")
    ranger_driver_curl_target = format("{seatunnel_lib}/{ranger_jdbc_jar_name}")
    ranger_previous_jdbc_jar = format("{seatunnel_lib}/{ranger_previous_jdbc_jar_name}")
    sql_connector_jar = ''

  protocol = "https" if seatunnel_web_ssl_enabled else "http"
  # Extract host information
  host = config['clusterHostInfo']['seatunnel-web_hosts'][0]
  # Construct the URL
  seatunnel_url = f"{protocol}://{host}:{web_server_port}/seatunnel/api/v1/"

  seatunnel_ranger_plugin_config = {
    'username': repo_config_username,
    'password': repo_config_password,
    'seatunnel.web.url': seatunnel_url,
    'commonNameForCertificate': common_name_for_certificate
  }

  if seatunnel_web_ssl_enabled:
    seatunnel_ranger_plugin_config.update({
      'seatunnel.web.ssl.truststore': seatunnel_web_ssl_keystore,
      'seatunnel.web.ssl.truststore.password': seatunnel_web_ssl_keystore_password,
      'seatunnel.web.ssl.truststore.type': seatunnel_web_ssl_keystore_type
    })

  if security_enabled:
    seatunnel_ranger_plugin_config['policy.download.auth.users'] = seatunnel_user
    seatunnel_ranger_plugin_config['tag.download.auth.users'] = seatunnel_user
    seatunnel_ranger_plugin_config['policy.grantrevoke.auth.users'] = seatunnel_user

  custom_ranger_service_config = generate_ranger_service_config(ranger_plugin_properties)
  if len(custom_ranger_service_config) > 0:
    seatunnel_ranger_plugin_config.update(custom_ranger_service_config)

  seatunnel_ranger_plugin_repo = {
    'isEnabled': 'true',
    'configs': seatunnel_ranger_plugin_config,
    'description': 'Seatunnel repo',
    'name': repo_name,
    'type': 'seatunnel'
  }

  xa_audit_db_password = ''
  if not is_empty(config['configurations']['admin-properties']['audit_db_password']) and stack_supports_ranger_audit_db and has_ranger_admin:
    xa_audit_db_password = config['configurations']['admin-properties']['audit_db_password']

  xa_audit_db_is_enabled = False
  if xml_configurations_supported and stack_supports_ranger_audit_db:
    xa_audit_db_is_enabled = config['configurations']['ranger-seatunnel-audit']['xasecure.audit.destination.db']

  xa_audit_hdfs_is_enabled = config['configurations']['ranger-seatunnel-audit']['xasecure.audit.destination.hdfs'] if xml_configurations_supported else False
  ssl_keystore_password = config['configurations']['ranger-seatunnel-policymgr-ssl']['xasecure.policymgr.clientssl.keystore.password'] if xml_configurations_supported else None
  ssl_truststore_password = config['configurations']['ranger-seatunnel-policymgr-ssl']['xasecure.policymgr.clientssl.truststore.password'] if xml_configurations_supported else None
  credential_file = format('/etc/ranger/{repo_name}/cred.jceks')

  # for SQLA explicitly disable audit to DB for Ranger
  if has_ranger_admin and stack_supports_ranger_audit_db and xa_audit_db_flavor.lower() == 'sqla':
    xa_audit_db_is_enabled = False

cluster_name = config['clusterName']
# ranger hive plugin section end




