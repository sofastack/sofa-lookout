/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.lookout.jvm;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.info.AutoPollFriendlyInfo;
import com.alipay.lookout.api.info.AutoPollSuggestion;
import com.alipay.lookout.spi.MetricsImporter;

import java.util.Map;
import java.util.Properties;

/**
 * Created by kevin.luy@alipay.com on 2017/5/23.
 */
public class JvmSystemPropertiesInfoMetricImporter implements MetricsImporter {

    @Override
    public void register(Registry registry) {
        Id id = registry.createId(LookoutIdNameConstants.JVM_SYSTEM_PROP_NAME);
        registry.info(id, new AutoPollFriendlyInfo<Properties>() {
            @Override
            public AutoPollSuggestion autoPollSuggest() {
                return AutoPollSuggestion.POLL_WHEN_UPDATED;
            }

            @Override
            public long lastModifiedTime() {
                //only report once on startup
                return -1;
            }

            @Override
            public Properties value() {
                return System.getProperties();
            }
        });

        Id envId = registry.createId(LookoutIdNameConstants.JVM_SYSTEM_ENV_NAME);
        registry.info(envId, new AutoPollFriendlyInfo<Map<String, String>>() {
            @Override
            public AutoPollSuggestion autoPollSuggest() {
                return AutoPollSuggestion.POLL_WHEN_UPDATED;
            }

            @Override
            public long lastModifiedTime() {
                //only report once on startup
                return -1;
            }

            @Override
            public Map<String, String> value() {
                return System.getenv();
            }
        });
    }
}
