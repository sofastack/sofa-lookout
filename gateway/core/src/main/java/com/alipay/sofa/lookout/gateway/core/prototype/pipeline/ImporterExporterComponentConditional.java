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
package com.alipay.sofa.lookout.gateway.core.prototype.pipeline;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.ConditionalOnExporterComponent;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.ConditionalOnImporterComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;

/**
 * select importers and exporters to activate
 *
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-09 10:06
 **/
public class ImporterExporterComponentConditional implements ConfigurationCondition {
    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(ImporterExporterComponentConditional.class);

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.PARSE_CONFIGURATION;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean active = isComponentEnable(context, metadata);
        return active;
    }

    private boolean isComponentEnable(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isImporter = metadata.isAnnotated(ConditionalOnImporterComponent.class.getName());

        MultiValueMap<String, Object> map = null;
        if (isImporter) {
            map = metadata.getAllAnnotationAttributes(ConditionalOnImporterComponent.class
                .getName());
        } else {
            map = metadata.getAllAnnotationAttributes(ConditionalOnExporterComponent.class
                .getName());
        }

        String importerExporterName = getFirstAnnotationValueFromMetadata(map, "value");
        MonitorComponent monitorComponent = getFirstAnnotationValueFromMetadata(map, "type");
        List<String> activeComponents = getActivatedComponentsFromConfiguration(context,
            isImporter, monitorComponent);
        boolean active = activeComponents.contains(importerExporterName);
        LOGGER.info("gateway {} {}:{} active:{}", monitorComponent.name().toLowerCase(),
            (isImporter ? "importer" : "exporter"), importerExporterName, active);
        return active;
    }

    private <T> T getFirstAnnotationValueFromMetadata(MultiValueMap<String, Object> map,
                                                      String attributeName) {
        List<Object> value = map.get(attributeName);
        if (CollectionUtils.isEmpty(value)) {
            return null;
        }
        return (T) value.get(0);
    }

    private List<String> getActivatedComponentsFromConfiguration(ConditionContext context,
                                                                 boolean isImporter,
                                                                 MonitorComponent monitorComponent) {
        Environment env = context.getEnvironment();
        String type = "";
        switch (monitorComponent) {
            case METRIC:
                type = "metrics";
                break;
            default:
                type = monitorComponent.name().toLowerCase();
        }
        String name = isImporter ? "importers" : "exporters";
        //configuration name:"metrics.importers"
        String importerLists = env.getProperty(type + "." + name);
        if (StringUtils.isNotEmpty(importerLists)) {
            return Arrays.asList(importerLists.split(","));
        }
        return null;
    }
}
