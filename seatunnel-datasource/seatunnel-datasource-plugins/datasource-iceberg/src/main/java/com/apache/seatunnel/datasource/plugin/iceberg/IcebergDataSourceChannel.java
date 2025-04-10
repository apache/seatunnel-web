package com.apache.seatunnel.datasource.plugin.iceberg;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IcebergDataSourceChannel implements DataSourceChannel {
    private transient Catalog catalog;

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return IcebergOptionRule.optionRule();
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return IcebergOptionRule.metadataRule();
    }

    @Override
    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        try {
            initCatalog(requestParams);
            List<String> tablesList = new ArrayList<>();
            Namespace namespace = Namespace.of(database);

            for (TableIdentifier identifier : catalog.listTables(namespace)) {
                tablesList.add(identifier.name());
            }

            return tablesList;
        } catch (Exception e) {
            throw new DataSourcePluginException("Failed to list tables in Iceberg", e);
        }
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try {
            initCatalog(requestParams);
            List<String> databasesList = new ArrayList<>();

            // Check if the catalog is an instance of HadoopCatalog
            if (catalog instanceof HadoopCatalog) {
                HadoopCatalog hadoopCatalog = (HadoopCatalog) catalog;

                for (Namespace namespace : hadoopCatalog.listNamespaces()) {
                    String dbName = namespace.level(0);
                    databasesList.add(dbName);
                }
            } else {
                throw new DataSourcePluginException("Unsupported catalog type");
            }

            return databasesList;
        } catch (Exception e) {
            throw new DataSourcePluginException("Failed to list databases in Iceberg", e);
        }
    }

    @Override
    public List<TableField> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull String table) {

        try {
            initCatalog(requestParams);
            TableIdentifier tableIdentifier = TableIdentifier.of(database, table);

            return catalog.loadTable(tableIdentifier).schema().asStruct().fields().stream()
                    .map(
                            field -> {
                                TableField tableField = new TableField();
                                tableField.setName(field.name());
                                tableField.setType(field.type().toString());
                                return tableField;
                            })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new DataSourcePluginException("Failed to get table fields from Iceberg", e);
        }
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try {
            initCatalog(requestParams);

            return true;
        } catch (Exception e) {

            throw new DataSourcePluginException(
                    "Check Iceberg connectivity failed, " + e.getMessage(), e);
        }
    }

    private void initCatalog(Map<String, String> requestParams) {
        if (catalog == null) {
            Configuration conf = new Configuration();
            String warehouseUri = requestParams.get(IcebergOptionRule.WAREHOUSE_URI.key());
            if (warehouseUri != null) {
                catalog = new HadoopCatalog(conf, warehouseUri);
            }
        }
    }
}
