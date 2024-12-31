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

package org.apache.seatunnel.datasource;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.common.utils.ExceptionUtils;
import org.apache.seatunnel.datasource.classloader.DatasourceClassLoader;
import org.apache.seatunnel.datasource.classloader.DatasourceLoadConfig;
import org.apache.seatunnel.datasource.exception.DataSourceSDKException;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.datasource.service.DataSourceService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public abstract class AbstractDataSourceClient implements DataSourceService {
    private static final String ST_WEB_BASEDIR_PATH = "ST_WEB_BASEDIR_PATH";
    //    private ClassLoader datasourceClassLoader; // thradlocal
    private final ThreadLocal<ClassLoader> datasourceClassLoader = new ThreadLocal<>();

    private final Map<String, DataSourcePluginInfo> supportedDataSourceInfo = new HashMap<>();

    private final Map<String, Integer> supportedDataSourceIndex = new HashMap<>();

    private final List<DataSourcePluginInfo> supportedDataSources = new ArrayList<>();

    private final List<DataSourceChannel> dataSourceChannels = new ArrayList<>();

    private final Map<String, DataSourceChannel> classLoaderChannel = new HashMap<>();

    protected AbstractDataSourceClient() {
        AtomicInteger dataSourceIndex = new AtomicInteger();
        for (String pluginName : DatasourceLoadConfig.pluginSet) {
            log.info("plugin set : " + pluginName);
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (DatasourceLoadConfig.classLoaderChannel.get(pluginName.toUpperCase()) != null) {
                log.info(pluginName + " is exist");
                continue;
            }
            Thread.currentThread().setContextClassLoader(getCustomClassloader(pluginName));
            try {
                Class<?> clazz =
                        Class.forName(
                                DatasourceLoadConfig.classLoaderFactoryName.get(
                                        pluginName.toUpperCase()),
                                true,
                                Thread.currentThread().getContextClassLoader());
                DataSourceFactory factory =
                        (DataSourceFactory) clazz.getDeclaredConstructor().newInstance();
                log.info("factory : " + factory);
                Set<DataSourcePluginInfo> dataSourcePluginInfos = factory.supportedDataSources();
                dataSourcePluginInfos.forEach(
                        dataSourceInfo -> {
                            supportedDataSourceInfo.put(
                                    dataSourceInfo.getName().toUpperCase(), dataSourceInfo);
                            supportedDataSourceIndex.put(
                                    dataSourceInfo.getName().toUpperCase(), dataSourceIndex.get());
                            supportedDataSources.add(dataSourceInfo);
                            log.info("factory : " + dataSourceInfo);
                        });
                DatasourceLoadConfig.classLoaderChannel.put(
                        pluginName.toUpperCase(), factory.createChannel());
                log.info(
                        DatasourceLoadConfig.classLoaderChannel
                                .get(pluginName.toUpperCase())
                                .toString());
            } catch (Exception e) {
                log.warn("datasource " + pluginName + " is error " + ExceptionUtils.getMessage(e));
            }
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        if (supportedDataSourceInfo.isEmpty()) {
            throw new DataSourceSDKException("No supported data source found");
        }
    }

    public Boolean isVirtualTableDatasource(String pluginName) {
        log.info("pluginName: {}", pluginName);
        return supportedDataSourceInfo.get(pluginName.toUpperCase()).getSupportVirtualTables();
    }

    @Override
    public Boolean checkDataSourceConnectivity(
            String pluginName, Map<String, String> dataSourceParams) {
        return executeByCustomerClassLoader(
                pluginName,
                () ->
                        getDataSourceChannel(pluginName)
                                .checkDataSourceConnectivity(pluginName, dataSourceParams));
    }

    @Override
    public List<DataSourcePluginInfo> listAllDataSources() {
        return supportedDataSources;
    }

    protected DataSourceChannel getDataSourceChannel(String pluginName) {
        checkNotNull(pluginName, "pluginName cannot be null");
        return DatasourceLoadConfig.classLoaderChannel.get(pluginName.toUpperCase());
    }

    @Override
    public OptionRule queryDataSourceFieldByName(String pluginName) {
        return executeByCustomerClassLoader(
                pluginName,
                () -> getDataSourceChannel(pluginName).getDataSourceOptions(pluginName));
    }

    @Override
    public OptionRule queryMetadataFieldByName(String pluginName) {
        return executeByCustomerClassLoader(
                pluginName,
                () ->
                        getDataSourceChannel(pluginName)
                                .getDatasourceMetadataFieldsByDataSourceName(pluginName));
    }

    @Override
    public List<String> getTables(
            String pluginName,
            String databaseName,
            Map<String, String> requestParams,
            Map<String, String> options) {
        return executeByCustomerClassLoader(
                pluginName,
                () ->
                        getDataSourceChannel(pluginName)
                                .getTables(pluginName, requestParams, databaseName, options));
    }

    @Override
    public List<String> getDatabases(String pluginName, Map<String, String> requestParams) {
        return executeByCustomerClassLoader(
                pluginName,
                () -> getDataSourceChannel(pluginName).getDatabases(pluginName, requestParams));
    }

    @Override
    public List<TableField> getTableFields(
            String pluginName,
            Map<String, String> requestParams,
            String databaseName,
            String tableName) {
        return executeByCustomerClassLoader(
                pluginName,
                () ->
                        getDataSourceChannel(pluginName)
                                .getTableFields(
                                        pluginName, requestParams, databaseName, tableName));
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            String pluginName,
            Map<String, String> requestParams,
            String databaseName,
            List<String> tableNames) {
        return executeByCustomerClassLoader(
                pluginName,
                () ->
                        getDataSourceChannel(pluginName)
                                .getTableFields(
                                        pluginName, requestParams, databaseName, tableNames));
    }

    @Override
    public Pair<String, String> getTableSyncMaxValue(
            String pluginName,
            Map<String, String> requestParams,
            String databaseName,
            String tableName,
            String updateFieldType) {
        return executeByCustomerClassLoader(
                pluginName,
                () ->
                        getDataSourceChannel(pluginName)
                                .getTableSyncMaxValue(
                                        pluginName,
                                        requestParams,
                                        databaseName,
                                        tableName,
                                        updateFieldType));
    }

    private ClassLoader getCustomClassloader(String pluginName) {
        String getenv =
                System.getenv(ST_WEB_BASEDIR_PATH) == null
                        ? System.getProperty(ST_WEB_BASEDIR_PATH)
                        : System.getenv(ST_WEB_BASEDIR_PATH);
        log.info("ST_WEB_BASEDIR_PATH is : " + getenv);
        String libPath = StringUtils.isEmpty(getenv) ? "/datasource" : (getenv + "/datasource");

        //        String libPath = "/root/apache-seatunnel-web-2.4.7-WS-SNAPSHOT/datasource/";
        File jarDirectory = new File(libPath);
        File[] jarFiles =
                jarDirectory.listFiles(
                        (dir, name) -> {
                            String pluginUpperCase = pluginName.toUpperCase();
                            String nameLowerCase = name.toLowerCase();
                            String pluginJar =
                                    DatasourceLoadConfig.classLoaderJarName.get(pluginUpperCase);
                            if (StringUtils.isEmpty(pluginJar)) {
                                log.warn(
                                        "classLoaderJarName get pluginUpperCase jar name is null : {} ",
                                        pluginUpperCase);
                            }
                            if (pluginUpperCase.equals("KAFKA")) {
                                return !nameLowerCase.contains("kingbase")
                                        && nameLowerCase.startsWith(
                                                DatasourceLoadConfig.classLoaderJarName.get(
                                                        pluginUpperCase));
                            } else {
                                return nameLowerCase.startsWith(
                                        DatasourceLoadConfig.classLoaderJarName.get(
                                                pluginUpperCase));
                            }
                        });

        log.info("jar file length :" + (jarFiles == null ? 0 : jarFiles.length));
        log.info(
                "jar file name :"
                        + (jarFiles == null
                                ? 0
                                : jarFiles.length == 0 ? "no jar" : jarFiles[0].getName()));
        DatasourceClassLoader customClassLoader =
                DatasourceLoadConfig.datasourceClassLoaders.get(pluginName.toUpperCase());
        try {
            if (customClassLoader == null) {
                jarFiles = jarFiles == null ? new File[0] : jarFiles;
                URL[] urls = new URL[jarFiles.length];
                for (int i = 0; i < jarFiles.length; i++) {
                    try {
                        urls[i] = jarFiles[i].toURI().toURL();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                customClassLoader =
                        new DatasourceClassLoader(
                                urls, Thread.currentThread().getContextClassLoader());
                DatasourceLoadConfig.datasourceClassLoaders.put(
                        pluginName.toUpperCase(), customClassLoader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("custom loader is:" + customClassLoader);
        return customClassLoader;
    }

    private void updateClassLoader(String pluginName) {
        log.info("update class loader");
        datasourceClassLoader.set(Thread.currentThread().getContextClassLoader());
        ClassLoader customClassLoader = getCustomClassloader(pluginName);
        Thread.currentThread().setContextClassLoader(customClassLoader);
        log.info(customClassLoader.toString());
    }

    private void classLoaderRestore() {
        try {
            log.info("close class loader");
            Thread.currentThread().setContextClassLoader(datasourceClassLoader.get());
        } catch (Exception e) {
            log.info("loader catch");
        }
    }

    @Override
    public Connection getConnection(String pluginName, Map<String, String> requestParams) {
        return executeByCustomerClassLoader(
                pluginName,
                () -> getDataSourceChannel(pluginName).getConnection(pluginName, requestParams));
    }

    /**
     * Execute the given {@code Callable} within the {@link ClassLoader} of the current thread.
     *
     * @param supplier
     * @param <T>
     * @return
     */
    @SneakyThrows
    private <T> T executeByCustomerClassLoader(String pluginName, @NonNull Supplier<T> supplier) {
        try {
            updateClassLoader(pluginName);
            return supplier.get();
        } finally {
            classLoaderRestore();
        }
    }
}
