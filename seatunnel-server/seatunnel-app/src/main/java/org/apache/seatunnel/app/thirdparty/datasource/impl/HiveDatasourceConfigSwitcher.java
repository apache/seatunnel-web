package org.apache.seatunnel.app.thirdparty.datasource.impl;

import org.apache.seatunnel.app.thirdparty.datasource.AbstractDataSourceConfigSwitcher;
import org.apache.seatunnel.app.thirdparty.datasource.DataSourceConfigSwitcher;

import com.google.auto.service.AutoService;

/**
 * * Hive: virtualTableDetail:null dataSourceOption: DataSourceOption(databases=[zd_zf],
 * tables=[cpaide_customer_sales_data_bak]) selectTableFields: SelectTableFields(all=true,
 * tableFields=[dt, grid_id, grid_name, zone_id, zone_name, channel_id, channel_name, salesman,
 * salesman_id, expansion_progress, customer_id, customer_name, demand_amount, supply_amount,
 * gross_profit_margin, business_profit, business_profit_margin, net_profit, net_profit_margin,
 * logistics_rate, storage_rate, personnel_rate, total_visit_count, recent_7days_visit_count,
 * first_order_visit_count, visit_calendar]) businessMode: DATA_INTEGRATION pluginType:SOURCE
 * connectorConfig:
 * Config(SimpleConfigObject({"table_name":"cpaide_customer_sales_data_bak","parallelism":1,"result_table_name":"Table11229265123808"}))
 * dataSourceInstanceConfig: Config(SimpleConfigObject({
 * "metastore_uri":"thrift://172.25.20.12:9083",
 * "kerberos_principal":"","kerberos_krb5_conf_path":"","kerberos_keytab_path":"",
 * "hdfs_site_path":"U:/export/server/conf/ZYCloud/hdfs-clientconfig/hadoop-conf/hdfs-site.xml",
 * "hive_site_path":"U:/export/server/conf/ZYCloud/hive-clientconfig/hive-conf/hive-site.xml"}))
 */

/** @author Mryan */
@AutoService(DataSourceConfigSwitcher.class)
public class HiveDatasourceConfigSwitcher extends AbstractDataSourceConfigSwitcher {
    @Override
    public String getDataSourceName() {
        return "HIVE";
    }
}
