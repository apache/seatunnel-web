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
package org.apache.seatunnel.app.bean.engine;

import org.apache.seatunnel.api.table.catalog.DataTypeConvertException;
import org.apache.seatunnel.api.table.catalog.DataTypeConvertor;
import org.apache.seatunnel.api.table.type.ArrayType;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EngineDataType {

    private static final Map<String, DataType> DATA_TYPE_MAP =
            Arrays.stream(DataType.values())
                    .collect(Collectors.toMap(DataType::getName, Function.identity()));

    public static List<DataType> getAllDataType() {
        return Arrays.asList(DataType.values());
    }

    public enum DataType {
        T_STRING("string", BasicType.STRING_TYPE),
        T_BOOLEAN("boolean", BasicType.BOOLEAN_TYPE),
        T_BYTE("tinyint", BasicType.BYTE_TYPE),
        T_SHORT("smallint", BasicType.SHORT_TYPE),
        T_INT("int", BasicType.INT_TYPE),
        T_LONG("bigint", BasicType.LONG_TYPE),
        T_FLOAT("float", BasicType.FLOAT_TYPE),
        T_DOUBLE("double", BasicType.DOUBLE_TYPE),
        T_VOID("null", BasicType.VOID_TYPE),

        T_DECIMAL("decimal(38, 18)", new DecimalType(38, 18)),

        T_LOCAL_DATE("date", LocalTimeType.LOCAL_DATE_TYPE),
        T_LOCAL_TIME("time", LocalTimeType.LOCAL_TIME_TYPE),
        T_LOCAL_DATE_TIME("timestamp", LocalTimeType.LOCAL_DATE_TIME_TYPE),

        T_PRIMITIVE_BYTE_ARRAY("bytes", PrimitiveByteArrayType.INSTANCE),

        T_STRING_ARRAY("array<string>", ArrayType.STRING_ARRAY_TYPE),
        T_BOOLEAN_ARRAY("array<boolean>", ArrayType.BOOLEAN_ARRAY_TYPE),
        T_BYTE_ARRAY("array<tinyint>", ArrayType.BYTE_ARRAY_TYPE),
        T_SHORT_ARRAY("array<smallint>", ArrayType.SHORT_ARRAY_TYPE),
        T_INT_ARRAY("array<int>", ArrayType.INT_ARRAY_TYPE),
        T_LONG_ARRAY("array<bigint>", ArrayType.LONG_ARRAY_TYPE),
        T_FLOAT_ARRAY("array<float>", ArrayType.FLOAT_ARRAY_TYPE),
        T_DOUBLE_ARRAY("array<double>", ArrayType.DOUBLE_ARRAY_TYPE);

        @Getter private final String name;
        @Getter private final SeaTunnelDataType<?> RawType;

        DataType(String name, SeaTunnelDataType<?> rawType) {
            this.name = name;
            this.RawType = rawType;
        }
    }

    /** This convertor is used to transform the data type from engine to connector. */
    public static class SeaTunnelDataTypeConvertor
            implements DataTypeConvertor<SeaTunnelDataType<?>> {

        @Override
        public SeaTunnelDataType<?> toSeaTunnelType(String engineDataType) {
            return DATA_TYPE_MAP.get(engineDataType.toLowerCase(Locale.ROOT)).getRawType();
        }

        @Override
        public SeaTunnelDataType<?> toSeaTunnelType(
                SeaTunnelDataType<?> seaTunnelDataType, Map<String, Object> map)
                throws DataTypeConvertException {
            return seaTunnelDataType;
        }

        @Override
        public SeaTunnelDataType<?> toConnectorType(
                SeaTunnelDataType<?> seaTunnelDataType, Map<String, Object> map)
                throws DataTypeConvertException {
            return seaTunnelDataType;
        }

        @Override
        public String getIdentity() {
            return "EngineDataTypeConvertor";
        }
    }
}
