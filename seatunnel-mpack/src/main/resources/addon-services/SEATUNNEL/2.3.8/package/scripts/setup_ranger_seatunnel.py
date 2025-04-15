#!/usr/bin/env python
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

# Ambari Commons & Resource Management Imports
from resource_management.core.logger import Logger
from resource_management.libraries.functions.setup_ranger_plugin_xml import setup_ranger_plugin

def setup_ranger_seatunnel(upgrade_type = None):
  import params

  if params.enable_ranger_seatunnel:
    stack_version = params.version

    if params.retryAble:
      Logger.info("Seatunnel: Setup ranger: command retry enables thus retrying if ranger admin is down !")
    else:
      Logger.info("Seatunnel: Setup ranger: command retry not enabled thus skipping if ranger admin is down !")

    if params.xa_audit_hdfs_is_enabled:
      try:
        params.HdfsResource("/ranger/audit",
                           type="directory",
                           action="create_on_execute",
                           owner=params.hdfs_user,
                           group=params.hdfs_user,
                           mode=0o755,
                           recursive_chmod=True
        )
        params.HdfsResource("/ranger/audit/seatunnel",
                           type="directory",
                           action="create_on_execute",
                           owner=params.seatunnel_user,
                           group=params.seatunnel_group,
                           mode=0o700,
                           recursive_chmod=True
        )
        params.HdfsResource(None, action="execute")
      except Exception as err:
        Logger.exception("Audit directory creation in HDFS for HIVE Ranger plugin failed with error:\n{0}".format(err))

    api_version='v2'

    setup_ranger_plugin('seatunnel-web', 'seatunnel', params.ranger_previous_jdbc_jar,
                        params.ranger_downloaded_custom_connector, params.ranger_driver_curl_source,
                        params.ranger_driver_curl_target, params.java64_home,
                        params.repo_name, params.seatunnel_ranger_plugin_repo,
                        params.ranger_env, params.ranger_plugin_properties,
                        params.policy_user, params.policymgr_mgr_url,
                        params.enable_ranger_seatunnel, conf_dict=params.seatunnel_web_conf_dir,
                        component_user=params.seatunnel_user, component_group=params.seatunnel_group, cache_service_list=['seatunnel-web'],
                        plugin_audit_properties=params.config['configurations']['ranger-seatunnel-audit'], plugin_audit_attributes=params.config['configurationAttributes']['ranger-seatunnel-audit'],
                        plugin_security_properties=params.config['configurations']['ranger-seatunnel-security'], plugin_security_attributes=params.config['configurationAttributes']['ranger-seatunnel-security'],
                        plugin_policymgr_ssl_properties=params.config['configurations']['ranger-seatunnel-policymgr-ssl'], plugin_policymgr_ssl_attributes=params.config['configurationAttributes']['ranger-seatunnel-policymgr-ssl'],
                        component_list=['seatunnel', 'seatunnel-web'], audit_db_is_enabled=params.xa_audit_db_is_enabled,
                        credential_file=params.credential_file, xa_audit_db_password=params.xa_audit_db_password,
                        ssl_truststore_password=params.ssl_truststore_password, ssl_keystore_password=params.ssl_keystore_password,
                        stack_version_override = stack_version, skip_if_rangeradmin_down= not params.retryAble, api_version=api_version,
                        is_security_enabled = params.security_enabled,
                        is_stack_supports_ranger_kerberos = params.stack_supports_ranger_kerberos,
                        component_user_principal= None,
                        component_user_keytab=None, cred_lib_path_override=params.cred_lib_path, cred_setup_prefix_override=params.cred_setup_prefix)
  else:
    Logger.info('Ranger Seatunnel plugin is not enabled')
