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
package com.alipay.lookout.starter.autoConfiguration;

import com.alipay.lookout.api.NoopRegistry;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.client.DefaultLookoutClient;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.AddressService;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.MetricObserver;
import com.alipay.lookout.starter.LookoutClientProperties;
import com.alipay.lookout.starter.support.reg.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.List;

import static com.alipay.lookout.core.config.LookoutConfig.*;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
@AutoConfigureOrder(-100)
@Configuration
@EnableConfigurationProperties(LookoutClientProperties.class)
@AutoConfigureBefore({ MetricsDropwizardAutoConfiguration.class })
public class LookoutAutoConfiguration implements BeanFactoryAware {
    private static final Logger                      logger = LoggerFactory
                                                                .getLogger(LookoutAutoConfiguration.class);

    @Autowired(required = false)
    private List<MetricObserver<LookoutMeasurement>> metricObservers;
    private BeanFactory                              beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Bean
    public LookoutConfig lookoutConfig(LookoutClientProperties lookoutClientProperties,
                                       Environment environment) {
        String appName = environment.getProperty("spring.application.name");
        Assert.notNull(appName, "spring.application.name can not be null!");
        LookoutConfig config = buildLookoutConfig(lookoutClientProperties);
        config.setProperty(APP_NAME, appName);
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.alipay.lookout.remote.step.LookoutRegistry")
    public AddressService lookoutAddressService(LookoutConfig config) {
        return LookoutRegistry.getAddressService(config);
    }

    @Bean
    @ConditionalOnClass(name = "com.alipay.lookout.remote.step.LookoutRegistry")
    public MetricsRegistryFactory lookoutServerRegistryFactory(AddressService addressService) {
        return new LookoutServerRegistryFactory(metricObservers, addressService);
    }

    @Bean
    @ConditionalOnClass(name = "com.alipay.lookout.reg.prometheus.PrometheusRegistry")
    public MetricsRegistryFactory prometheusMetricsRegistryFactory() {
        return new PrometheusMetricsRegistryFactory();
    }

    /**
     * why use beanFactory to get DropWizardMetricsRegistry bean here?
     * because we do not want to import dropwizard dependencies indirectly!
     * let the application developer to decide whether or not to import.
     *
     * @return
     */
    @Deprecated
    @Bean
    @ConditionalOnProperty(prefix = "com.alipay.sofa.lookout", name = "actuator-dropWizard-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = { "com.alipay.lookout.dropwizard.metrics.DropWizardMetricsRegistry",
            "com.codahale.metrics.MetricRegistry" })
    public DropWizardMetricsRegistryFactory dropWizardMetricsRegistryFactory() {
        try {
            /*
             * In order to avoid [com.codahale.metrics.MetricRegistry] class not found.
             */
            com.codahale.metrics.MetricRegistry metricRegistry = beanFactory
                .getBean(com.codahale.metrics.MetricRegistry.class);
            if (metricRegistry == null) {
                logger
                    .warn("spring boot actuator does not use dropwizard service,so lookout ignore dropwizard too!");
            } else {
                return new DropWizardMetricsRegistryFactory(metricRegistry);
            }
        } catch (NoClassDefFoundError e) {
            logger.debug("no dropwizard service found.");
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean({ DropWizardMetricsRegistryFactory.class })
    @ConditionalOnClass(name = "org.springframework.boot.actuate.metrics.Metric")
    public SpringBootActuatorRegistryFactory springBootActuatorServerRegistryFactory() {
        return new SpringBootActuatorRegistryFactory();
    }

    @Bean
    public Registry registry(List<MetricsRegistryFactory> metricsRegistryFactoryList,
                             LookoutConfig lookoutConfig) {
        if (!lookoutConfig.getBoolean(LOOKOUT_ENABLE, true)) {
            return NoopRegistry.INSTANCE;
        }
        DefaultLookoutClient lookoutClient = new DefaultLookoutClient(
            lookoutConfig.getString(APP_NAME));
        for (MetricsRegistryFactory metricsRegistryFactory : metricsRegistryFactoryList) {
            lookoutClient.addRegistry(metricsRegistryFactory.get(lookoutConfig));
        }
        logger.info("register extended metrics.");
        lookoutClient.registerExtendedMetrics();

        if (lookoutConfig.getBoolean(LookoutConfig.POLLER_EXPORTER_ENABLED, true)) {
            lookoutClient.registerExporter(lookoutConfig);
        }

        return lookoutClient.getRegistry();
    }

    protected LookoutConfig buildLookoutConfig(LookoutClientProperties lookoutClientProperties) {
        LookoutConfig lookoutConfig = new LookoutConfig();
        lookoutConfig.setProperty(LOOKOUT_ENABLE, lookoutClientProperties.isEnable());
        lookoutConfig.setProperty(LOOKOUT_AGENT_HOST_ADDRESS,
            lookoutClientProperties.getAgentHostAddress());
        lookoutConfig.setProperty(LOOKOUT_MAX_METRICS_NUMBER,
            lookoutClientProperties.getMaxMetricsNum());
        lookoutConfig.setProperty(LOOKOUT_REPORT_BATCH_SIZE,
            lookoutClientProperties.getReportBatchSize());
        lookoutConfig.setProperty(LOOKOUT_AUTOPOLL_ENABLE,
            lookoutClientProperties.isAutopollEnable());
        lookoutConfig.setProperty(LOOKOUT_AUTOPOLL_INFO_METRIC_IGNORE,
            lookoutClientProperties.isAutopollInfoIgnore());
        lookoutConfig.setProperty(LOOKOUT_AGENT_SERVER_PORT,
            lookoutClientProperties.getAgentServerPort());
        lookoutConfig.setProperty(LOOKOUT_PROMETHEUS_EXPORTER_SERVER_PORT,
            lookoutClientProperties.getPrometheusExporterServerPort());
        if (lookoutClientProperties.getPollingInterval() > 0) {
            lookoutConfig.setStepInterval(PRIORITY.NORMAL,
                lookoutClientProperties.getPollingInterval());
        }
        return lookoutConfig;
    }
}
