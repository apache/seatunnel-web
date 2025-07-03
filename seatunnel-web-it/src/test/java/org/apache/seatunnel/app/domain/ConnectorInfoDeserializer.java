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
package org.apache.seatunnel.app.domain;

import org.apache.seatunnel.api.common.PluginIdentifier;
import org.apache.seatunnel.app.domain.response.connector.ConnectorInfo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ConnectorInfoDeserializer extends JsonDeserializer<ConnectorInfo> {

    @Override
    public ConnectorInfo deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        JsonNode pluginIdentifierNode = node.get("pluginIdentifier");
        String artifactId = node.get("artifactId").asText();

        PluginIdentifier pluginIdentifier =
                new PluginIdentifierDeserializer()
                        .deserialize(
                                pluginIdentifierNode.traverse(jsonParser.getCodec()),
                                deserializationContext);

        return new ConnectorInfo(pluginIdentifier, artifactId);
    }
}
