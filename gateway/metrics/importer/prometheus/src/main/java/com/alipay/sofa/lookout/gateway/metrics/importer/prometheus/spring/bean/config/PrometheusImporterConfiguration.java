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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.spring.bean.config;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.ConditionalOnImporterComponent;
import com.alipay.sofa.lookout.gateway.core.scrape.DefaultScrapeManager;
import com.alipay.sofa.lookout.gateway.core.scrape.JobBuilder;
import com.alipay.sofa.lookout.gateway.core.scrape.JobConfigResolver;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeManager;
import com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.PrometheusMetricImporter;
import com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.scrape.PromJobConfigResolver;
import com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.scrape.PromScrapeJobBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/26
 */
@ConditionalOnImporterComponent(value = "prometheus", type = MonitorComponent.METRIC)
@Configuration
public class PrometheusImporterConfiguration {
    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(PrometheusImporterConfiguration.class);

    @Bean
    RouterFunction<ServerResponse> prometheusImporter_router() {
        RequestPredicate predicate = PUT("/prom/metrics/job/{job}/**")
                .or(POST("/prom/metrics/job/{job}/**"));
        HandlerFunction<ServerResponse> handler = prometheusMetricImporter()::handle;
        return RouterFunctions.route(predicate, handler);
    }

    @Bean
    public PrometheusMetricImporter prometheusMetricImporter() {
        return new PrometheusMetricImporter();
    }

    @Bean
    RouterFunction<ServerResponse> fetchPromTargets_router() {
        RequestPredicate predicate = GET("/prom/targets");
        HandlerFunction<ServerResponse> handler = prometheusMetricImporter()::fetchTargets;
        return RouterFunctions.route(predicate, handler);
    }

    @Bean
    public ScrapeManager metricScrapeMananger() {
        PrometheusMetricImporter prometheusMetricImporter = prometheusMetricImporter();
        JobConfigResolver jobConfigResolver = new PromJobConfigResolver();
        JobBuilder jobBuilder = new PromScrapeJobBuilder(result -> {
            prometheusMetricImporter.pull(result);
        });
        ScrapeManager manager = new DefaultScrapeManager(MonitorComponent.METRIC, jobConfigResolver, jobBuilder);
        prometheusMetricImporter.setScrapeMananger(manager);

        //TODO 增加固定classpath下（抓取配置）一次性扫描解析，读取功能(Built-In)。
        //增加固定服务目录扫描（抓取配置）功能
        String configPath = getLookoutConfigFilePath() + "/metric_scrape";

        manager.watchFreshScrapeConfigs(() -> {
            try {
                manager.updateScrapeConfigs(manager.loadConfigFileFromFilePath(configPath));
            } catch (FileNotFoundException e) {
                LOGGER.warn("scan scrape config files fail.exception:{}", e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
        return manager;

    }

    private String getLookoutConfigFilePath() {
        String configPath = System.getProperty("config.dir");
        if (StringUtils.isEmpty(configPath)) {
            configPath = System.getProperty("user.home") + "/config";
        }
        return configPath;
    }
}
