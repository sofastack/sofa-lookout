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
package com.alipay.sofa.lookout.gateway.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author kevin.luy@antfin.com
 * @create 2018-12-01 11:08 AM
 **/
public class MonitorComponentConditional implements ConfigurationCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorComponentConditional.class);

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.PARSE_CONFIGURATION;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        MultiValueMap<String, Object> map = metadata
            .getAllAnnotationAttributes(ConditionalOnMonitorComponent.class.getName());
        List<Object> value = map.get("value");
        if (CollectionUtils.isEmpty(value)) {
            return false;
        }
        String componentName = (String) value.get(0);
        List<String> activeComponents = getComponents(context);
        boolean active = activeComponents.contains(componentName);

        active = active ? isActiveByZoneInfo(env, componentName) : false;

        LOGGER.info("monitor component:{}, active:{}", componentName, active);
        return active;
    }

    private boolean isActiveByZoneInfo(Environment env, String componentName) {
        String zonesKey = componentName + ".zones.active";
        String activeZones = env.getProperty(zonesKey);
        boolean active = true;
        if (!StringUtils.isEmpty(activeZones)) {
            String currentZone = System.getProperty("com.alipay.ldc.zone");
            LOGGER.info("zonesKey={} activeZones={} currentZone={}", zonesKey, activeZones,
                currentZone);
            // -Dcom.alipay.ldc.zone=GZ00C
            active = currentZone == null || activeZones.contains(currentZone);
        }
        return active;
    }

    private List<String> getComponents(ConditionContext context) {
        Environment env = context.getEnvironment();
        String componentList = env.getProperty("components.active");
        if (componentList == null) {
            componentList = "metric";
        }
        return Arrays.asList(componentList.split(","));
    }
}
