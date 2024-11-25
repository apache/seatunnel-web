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
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DorisConnection {
    public static void main(String[] args) {
        String user = "root";
        String password = "root";
        String newUrl =
                "jdbc:mysql://127.0.0.1:9030/test?useUnicode=true&characterEncoding=utf8&useTimezone=true&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        try {
            Connection myCon = DriverManager.getConnection(newUrl, user, password);
            Statement stmt = myCon.createStatement();
            ResultSet result = stmt.executeQuery("show databases");
            ResultSetMetaData metaData = result.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(result.getObject(i));
                }
            }
        } catch (SQLException e) {
            log.error("get JDBC connection exception.", e);
        }
    }
}
