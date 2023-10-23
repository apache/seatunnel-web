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

import java.util.List;

@AutoService(DataSourceConfigSwitcher.class)
public class DorisDatasourceConfigSwitcher extends AbstractDataSourceConfigSwitcher {

    private static final String TABLE = "table.identifier";

    @Override
    public String getDataSourceName() {
        return "DORIS";
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
        if (PluginType.SOURCE.equals(pluginType)) {
            throw new UnsupportedOperationException("Unsupported PluginType: " + pluginType);
        } else if (PluginType.SINK.equals(pluginType)) {
            excludedKeys.add(TABLE);
        } else {
            throw new UnsupportedOperationException("Unsupported plugin type: " + pluginType);
        }
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

    @Override
    public Config mergeDatasourceConfig(
            Config dataSourceInstanceConfig,
            VirtualTableDetailRes virtualTableDetail,
            DataSourceOption dataSourceOption,
            SelectTableFields selectTableFields,
            BusinessMode businessMode,
            PluginType pluginType,
            Config connectorConfig) {
        if (PluginType.SOURCE.equals(pluginType)) {
            throw new UnsupportedOperationException("Unsupported PluginType: " + pluginType);
        } else if (PluginType.SINK.equals(pluginType)) {
            if (businessMode.equals(BusinessMode.DATA_INTEGRATION)) {
                connectorConfig =
                        connectorConfig.withValue(
                                TABLE,
                                ConfigValueFactory.fromAnyRef(
                                        dataSourceOption
                                                .getDatabases()
                                                .get(0)
                                                .concat(".")
                                                .concat(dataSourceOption.getTables().get(0))));
            }
        } else {
            throw new UnsupportedOperationException("Unsupported plugin type: " + pluginType);
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
