package org.apache.seatunnel.app.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.core.starter.utils.ConfigShadeUtils;

import java.util.Map;

@Slf4j
public class ConfigShadeUtil {

    public static void encryptData(Map<String, String> datasourceConfig, String datasourceEncryptionType) {
        String password = datasourceConfig.get(Constants.PASSWORD);
        if(!password.isEmpty()) {
            try {
                datasourceConfig.replace(
                        Constants.PASSWORD,
                        ConfigShadeUtils.encryptOption(
                                datasourceEncryptionType, password));
            } catch (IllegalArgumentException ex) {
                log.warn("encrypt password failed");
            }
        }
    }

    public static void decryptData(Map<String, String> datasourceConfig, String datasourceEncryptionType) {
        String password = datasourceConfig.get(Constants.PASSWORD);
        if(!password.isEmpty()) {
            try {
                datasourceConfig.replace(
                        Constants.PASSWORD,
                        ConfigShadeUtils.decryptOption(
                                datasourceEncryptionType, password));
            } catch (IllegalArgumentException ex) {
                log.warn("decrypt password failed as password is not encrypted");
            }
        }
    }


}
