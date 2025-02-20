/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.common.replication;

import java.util.HashMap;
import java.util.Map;

public class ReplicationStrategyFactory {

    private static final Map<String, Class<? extends IReplicationStrategy>> BUILT_IN_REPLICATION_STRATEGY =
            new HashMap<>();

    static {
        BUILT_IN_REPLICATION_STRATEGY.put("none", NoReplicationStrategy.class);
        BUILT_IN_REPLICATION_STRATEGY.put("all", AllDatasetsReplicationStrategy.class);
        BUILT_IN_REPLICATION_STRATEGY.put("metadata", MetadataOnlyReplicationStrategy.class);
    }

    private ReplicationStrategyFactory() {
        throw new AssertionError();
    }

    public static IReplicationStrategy create(String name) {
        String strategyName = name.toLowerCase();
        if (!BUILT_IN_REPLICATION_STRATEGY.containsKey(strategyName)) {
            throw new IllegalStateException("Couldn't find strategy with name: " + name);
        }
        Class<? extends IReplicationStrategy> clazz = BUILT_IN_REPLICATION_STRATEGY.get(strategyName);
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Couldn't instantiated replication strategy: " + name, e);
        }
    }
}
