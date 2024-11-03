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

package org.apache.seatunnel.app.thirdparty.datasource.impl;

import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigValueFactory;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.configuration.util.RequiredOption;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableDetailRes;
import org.apache.seatunnel.app.dynamicforms.FormStructure;
import org.apache.seatunnel.app.thirdparty.datasource.AbstractDataSourceConfigSwitcher;
import org.apache.seatunnel.app.thirdparty.datasource.DataSourceConfigSwitcher;
import org.apache.seatunnel.common.constants.PluginType;

import com.google.auto.service.AutoService;

import java.util.Arrays;
import java.util.List;

@AutoService(DataSourceConfigSwitcher.class)
public class HiveDataSourceConfigSwitcher extends AbstractDataSourceConfigSwitcher {
    private static final String METASTORE_URI = "metastore_uri";
    private static final String KERBEROS_PRINCIPAL = "kerberos_principal";
    private static final String KERBEROS_KEYTAB_PATH = "kerberos_keytab_path";
    private static final String KERBEROS_KRB5_CONF_PATH = "kerberos_krb5_conf_path";
    private static final String HDFS_SITE_PATH = "hdfs_site_path";
    private static final String HIVE_SITE_PATH = "hive_site_path";
    private static final String HIVE_HADOOP_CONF = "hive.hadoop.conf";
    private static final String HIVE_HADOOP_CONF_PATH = "hive.hadoop.conf-path";
    private static final String TABLE_NAME = "table_name";
    private static final List<String> excludes =
            Arrays.asList(
                    METASTORE_URI,
                    KERBEROS_PRINCIPAL,
                    KERBEROS_KEYTAB_PATH,
                    KERBEROS_KRB5_CONF_PATH,
                    HDFS_SITE_PATH,
                    HIVE_SITE_PATH,
                    TABLE_NAME,
                    HIVE_HADOOP_CONF,
                    HIVE_HADOOP_CONF_PATH);

    @Override
    public String getDataSourceName() {
        return "HIVE";
    }

    @Override
    public FormStructure filterOptionRule(
            String connectorName,
            OptionRule dataSourceOptionRule,
            OptionRule virtualTableOptionRule,
            BusinessMode businessMode,
            PluginType pluginType,
            OptionRule connectorOptionRule,
            List<RequiredOption> addRequiredOptions,
            List<Option<?>> addOptionalOptions,
            List<String> excludedKeys) {
        return super.filterOptionRule(
                connectorName,
                dataSourceOptionRule,
                virtualTableOptionRule,
                businessMode,
                pluginType,
                connectorOptionRule,
                addRequiredOptions,
                addOptionalOptions,
                excludes);
    }

    @Override
    public Config mergeDatasourceConfig(
            Config dataSourceInstanceConfig,
            VirtualTableDetailRes virtualTableDetail,
            DataSourceOption dataSourceOption,
            SelectTableFields selectTableFields,
            BusinessMode businessMode,
            PluginType pluginType,
            Config connectorConfig) {

        if (dataSourceOption.getDatabases().size() == 1
                && dataSourceOption.getTables().size() == 1) {
            connectorConfig =
                    connectorConfig.withValue(
                            TABLE_NAME,
                            ConfigValueFactory.fromAnyRef(
                                    dataSourceOption.getDatabases().get(0)
                                            + "."
                                            + dataSourceOption.getTables().get(0)));
        }

        return super.mergeDatasourceConfig(
                dataSourceInstanceConfig,
                virtualTableDetail,
                dataSourceOption,
                selectTableFields,
                businessMode,
                pluginType,
                connectorConfig);
    }
}
