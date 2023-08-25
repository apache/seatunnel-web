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

package com.whaleops.datasource.datasource.plugin.hive.client;

import org.apache.seatunnel.common.utils.ExceptionUtils;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.thrift.TException;

import com.whaleops.datasource.datasource.plugin.hive.HiveOptionRule;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class HiveClient implements AutoCloseable {

    private final HiveMetaStoreClient hiveMetaStoreClient;

    public HiveClient(HiveMetaStoreClient hiveMetaStoreClient) {
        this.hiveMetaStoreClient = hiveMetaStoreClient;
    }

    public static HiveClient createInstance(Map<String, String> reqParam) {
        checkNotNull(
                reqParam.get(HiveOptionRule.METASTORE_URI.key()),
                "hive metastore_uri cannot be null");
        String metastoreUri = reqParam.get(HiveOptionRule.METASTORE_URI.key());
        String kerberosPrincipal = reqParam.get(HiveOptionRule.KERBEROS_PRINCIPAL.key());
        String kerberosKrb5ConfPath = reqParam.get(HiveOptionRule.KERBEROS_KRB5_CONF_PATH.key());
        String kerberosKeytabPath = reqParam.get(HiveOptionRule.KERBEROS_KEYTAB_PATH.key());
        String hdfsSitePath = reqParam.get(HiveOptionRule.HDFS_SITE_PATH.key());
        String hiveSitePath = reqParam.get(HiveOptionRule.HIVE_SITE_PATH.key());
        System.setProperty("java.security.krb5.conf", kerberosKrb5ConfPath);
        System.setProperty("krb.principal", "hadoop");
        try {
            if (StringUtils.isNotEmpty(kerberosPrincipal)) {
                // login Kerberos
                Configuration configuration = new Configuration();
                if (StringUtils.isNotEmpty(kerberosPrincipal)) {
                    configuration.addResource(new File(hdfsSitePath).toURI().toURL());
                }
                doKerberosAuthentication(configuration, kerberosPrincipal, kerberosKeytabPath);
            }
            HiveConf hiveConf = new HiveConf();
            hiveConf.set("hive.metastore.uris", metastoreUri);
            if (StringUtils.isNotEmpty(hiveSitePath)) {
                hiveConf.addResource(new File(hiveSitePath).toURI().toURL());
            }
            log.info("hive client conf:{}", hiveConf);

            return new HiveClient(new HiveMetaStoreClient(hiveConf));
        } catch (Exception e) {
            String errorMsg =
                    String.format(
                            "Using this hive uris [%s] to initialize "
                                    + "hive metastore client instance failed",
                            metastoreUri);
            log.error(ExceptionUtils.getMessage(e));
            throw new DataSourcePluginException(errorMsg, e);
        }
    }

    /*private static void authKerberos(
            String kerberosKrb5ConfPath, String kerberosKeytabPath, String kerberosPrincipal)
            throws IOException {
        System.setProperty("java.security.krb5.conf", kerberosKrb5ConfPath);
        Configuration configuration = new Configuration();
        configuration.set("hadoop.security.authentication", "Kerberos");
        configuration.setBoolean("hadoop.security.authorization", true);
        UserGroupInformation.setConfiguration(configuration);
        UserGroupInformation.loginUserFromKeytab(kerberosPrincipal, kerberosKeytabPath);
    }*/

    public static void doKerberosAuthentication(
            Configuration configuration, String principal, String keytabPath) {
        if (StringUtils.isBlank(principal) || StringUtils.isBlank(keytabPath)) {
            log.warn(
                    "Principal [{}] or keytabPath [{}] is empty, it will skip kerberos authentication",
                    principal,
                    keytabPath);
        } else {
            configuration.set("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(configuration);
            try {
                log.info(
                        "Start Kerberos authentication using principal {} and keytab {}",
                        principal,
                        keytabPath);
                UserGroupInformation.loginUserFromKeytab(principal, keytabPath);
                log.info("Kerberos authentication successful");
            } catch (IOException e) {
                throw new DataSourcePluginException(
                        "check hive connectivity failed, " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
        try {
            if (hiveMetaStoreClient != null) {
                hiveMetaStoreClient.close();
            }
        } catch (Exception e) {
            log.warn("close hive connection error", e);
        }
    }

    public List<String> getAllDatabases() {
        try {
            return hiveMetaStoreClient.getAllDatabases();
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e));
            throw new DataSourcePluginException("get database names failed", e);
        }
    }

    public List<String> getAllTables(String dbName) {
        return getAllTables(dbName, null, null);
    }

    public List<String> getAllTables(String dbName, String filterName, Integer size) {
        try {

            List<String> tables = hiveMetaStoreClient.getTables(dbName, "*");
            List<String> filteredTables = new ArrayList<>();
            // hive api whether to support filtering directly
            if (StringUtils.isNotEmpty(filterName)) {
                for (String tableName : tables) {
                    if (tableName.contains(filterName)) {
                        filteredTables.add(tableName);
                    }
                }
            } else {
                filteredTables.addAll(tables);
            }
            // filter Iceberg table
            this.filteredIcebergTable(filteredTables, dbName);
            if (size != null && size > 0) {
                return filteredTables.subList(0, Math.min(size, filteredTables.size()));
            }
            return filteredTables;
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e));
            throw new DataSourcePluginException("get table names failed", e);
        }
    }

    // 过滤掉 Iceberg 表
    private void filteredIcebergTable(List<String> filteredTables, String dbName)
            throws TException {
        if (CollectionUtils.isNotEmpty(filteredTables)) {
            Iterator<String> iterator = filteredTables.iterator();
            while (iterator.hasNext()) {
                String element = iterator.next();
                if (isIcebergTable(dbName, element)) {
                    iterator.remove();
                }
            }
        }
    }

    // 判断是否为 Iceberg 表
    private boolean isIcebergTable(String dbName, String tableName) throws TException {
        Table table = hiveMetaStoreClient.getTable(dbName, tableName);
        Map<String, String> parameters = table.getParameters();
        String tableType = parameters.get("table_type");
        return "ICEBERG".equals(tableType);
    }

    public List<TableField> getFields(String dbName, String tableName) {
        List<TableField> tableFields = new ArrayList<>();
        try {
            List<FieldSchema> fields = hiveMetaStoreClient.getFields(dbName, tableName);
            fields.forEach(
                    field -> {
                        TableField tableField = new TableField();
                        String columnName = field.getName();
                        tableField.setPrimaryKey(false);
                        tableField.setName(columnName);
                        String stringType = field.getType();
                        String fieldTypeStart = stringType.split("[(<]")[0];
                        tableField.setType(fieldTypeStart);
                        tableField.setComment(field.getComment());
                        tableFields.add(tableField);
                    });
            return tableFields;
        } catch (TException e) {
            log.error(ExceptionUtils.getMessage(e));
            throw new DataSourcePluginException("get table fields failed", e);
        }
    }
}
