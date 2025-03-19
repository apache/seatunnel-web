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

package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.app.config.EncryptionConfig;
import org.apache.seatunnel.core.starter.utils.ConfigShadeUtils;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static org.apache.seatunnel.app.common.Constants.ENCRYPTION_TYPE_NONE;

@Slf4j
@Component
public class ConfigShadeUtil {

    @Autowired private EncryptionConfig encryptionConfig;

    public void encryptData(Map<String, String> datasourceConfig) {
        if (encryptionConfig.getType().equals(ENCRYPTION_TYPE_NONE)) {
            return;
        }
        for (String key : encryptionConfig.getKeysToEncrypt()) {
            String value = datasourceConfig.get(key);
            if (StringUtils.isNotEmpty(value)) {
                try {
                    String processedValue =
                            ConfigShadeUtils.encryptOption(encryptionConfig.getType(), value);
                    datasourceConfig.replace(key, processedValue);
                } catch (IllegalArgumentException ex) {
                    log.error("encryption for key {} failed", key);
                    throw new SeatunnelException(
                            SeatunnelErrorEnum.ERROR_CONFIG,
                            String.format(
                                    "encryption failed for key: %s, check if the keys were persisted in expected format",
                                    key),
                            ex);
                }
            }
        }
    }

    public void decryptData(Map<String, String> datasourceConfig) {
        if (encryptionConfig.getType().equals(ENCRYPTION_TYPE_NONE)) {
            return;
        }
        for (String key : encryptionConfig.getKeysToEncrypt()) {
            String value = datasourceConfig.get(key);
            if (StringUtils.isNotEmpty(value)) {
                try {
                    String processedValue =
                            ConfigShadeUtils.decryptOption(encryptionConfig.getType(), value);
                    datasourceConfig.replace(key, processedValue);
                } catch (IllegalArgumentException ex) {
                    log.error("decryption for key {} failed", key);
                    throw new SeatunnelException(
                            SeatunnelErrorEnum.ERROR_CONFIG,
                            String.format(
                                    "decryption failed for key: %s, check if the keys were persisted in expected format",
                                    key),
                            ex);
                }
            }
        }
    }
}
