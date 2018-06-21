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
package com.alipay.lookout.starter.configuration;

import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.starter.autoConfiguration.LookoutAutoConfiguration;
import com.alipay.lookout.starter.support.actuator.LookoutSpringBootMetricsImpl;
import com.alipay.lookout.starter.support.actuator.SpringBootActuatorRegistry;
import com.alipay.lookout.starter.support.reader.LookoutRegistryMetricReader;
import com.alipay.lookout.starter.support.reg.SpringBootActuatorRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.MetricReaderPublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * use lookout registry as the default registry of spring boot actuator
 * Created by kevin.luy@alipay.com on 2018/6/21.
 */
@AutoConfigureAfter(LookoutAutoConfiguration.class)
@Configuration
@ConditionalOnBean(SpringBootActuatorRegistryFactory.class)
@ConditionalOnClass(name = { "org.springframework.boot.actuate.metrics.GaugeService" })
public class MetricsActuatorAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(LookoutAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean({ LookoutSpringBootMetricsImpl.class, CounterService.class,
            GaugeService.class })
    public LookoutSpringBootMetricsImpl lookoutMetricServices(Registry lookoutMetricRegistry) {
        logger.info("Spring Boot Metrics binding to SOFALookout Implementation!");
        return new LookoutSpringBootMetricsImpl(lookoutMetricRegistry);
    }

    @Bean
    public LookoutRegistryMetricReader lookoutRegistryMetricReader(SpringBootActuatorRegistryFactory springBootActuatorRegistryFactory,
                                                                   LookoutConfig lookoutConfig) {
        SpringBootActuatorRegistry springBootActuatorRegistry = springBootActuatorRegistryFactory
            .get(lookoutConfig);
        return new LookoutRegistryMetricReader(springBootActuatorRegistry);
    }

    @Bean
    @ConditionalOnBean(LookoutRegistryMetricReader.class)
    public MetricReaderPublicMetrics lookoutPublicMetrics(LookoutRegistryMetricReader lookoutRegistryMetricReader) {
        return new MetricReaderPublicMetrics(lookoutRegistryMetricReader);
    }
}
