package org.apache.seatunnel.app.thirdparty.datasource.impl;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@AutoService(DataSourceConfigSwitcher.class)
public class IcebergDataSourceConfigSwitcher extends AbstractDataSourceConfigSwitcher {
    private static final String WAREHOUSE_URI = "warehouse";
    private static final String CATALOG_TYPE = "catalog_type";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(IcebergDataSourceConfigSwitcher.class);

    @Override
    public String getDataSourceName() {
        return "ICEBERG";
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
        // 根据需要添加或排除特定的配置选项
        return super.filterOptionRule(
                connectorName,
                dataSourceOptionRule,
                virtualTableOptionRule,
                businessMode,
                pluginType,
                connectorOptionRule,
                addRequiredOptions,
                addOptionalOptions,
                excludedKeys);
    }

    private void logToFile(String message) {
        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(new File("/home/log.log"), true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
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
        logToFile("0dataSourceInstanceConfig:" + dataSourceInstanceConfig);
        logToFile("0virtualTableDetail:" + virtualTableDetail);
        logToFile("0dataSourceOption:" + dataSourceOption);
        logToFile("0selectTableFields:" + selectTableFields);
        logToFile("0businessMode:" + businessMode);
        logToFile("0pluginType:" + pluginType);
        logToFile("0connectorConfig:" + connectorConfig);

        LOGGER.info("0dataSourceInstanceConfig:" + dataSourceInstanceConfig);
        LOGGER.info("0virtualTableDetail:" + virtualTableDetail);
        LOGGER.info("0dataSourceOption:" + dataSourceOption);
        LOGGER.info("0selectTableFields:" + selectTableFields);
        LOGGER.info("0businessMode:" + businessMode);
        LOGGER.info("0pluginType:" + pluginType);
        LOGGER.info("0connectorConfig:" + connectorConfig);
        // 确保 warehouse 和 catalog_type 配置项存在
        //        if (!dataSourceInstanceConfig.hasPath(WAREHOUSE_URI)
        //                || !dataSourceInstanceConfig.hasPath(CATALOG_TYPE)) {
        //            throw new IllegalArgumentException(
        //                    "Missing required configuration for 'warehouse' or 'catalog_type'");
        //        }
        //
        //        // 直接从 dataSourceInstanceConfig 获取配置项
        //        connectorConfig =
        //                connectorConfig.withValue(
        //                        WAREHOUSE_URI,
        //                        ConfigValueFactory.fromAnyRef(
        //                                dataSourceInstanceConfig.getString(WAREHOUSE_URI)));
        //        connectorConfig =
        //                connectorConfig.withValue(
        //                        CATALOG_TYPE,
        //                        ConfigValueFactory.fromAnyRef(
        //                                dataSourceInstanceConfig.getString(CATALOG_TYPE)));

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
