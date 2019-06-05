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
package com.alipay.sofa.lookout.gateway.metrics.importer.metricbeat.spring.bean.config;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.ConditionalOnImporterComponent;
import com.alipay.sofa.lookout.gateway.metrics.importer.metricbeat.MetricbeatMetricImporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/27
 */
@ConditionalOnImporterComponent(value = "metricbeat", type = MonitorComponent.METRIC)
@Configuration
public class MetricbeatImporterConfiguration {
    @Bean
    public RouterFunction<ServerResponse> metricbeat_importer_metadata_router() {
        RequestPredicate predicate = GET("/beat");
        HandlerFunction<ServerResponse> handler = metricbeatMetricImporter()::getMetadata;
        return route(predicate, handler);
    }

    @Bean
    public RouterFunction<ServerResponse> metricbeat_importer_template_router() {
        RequestPredicate predicate = GET("/beat/_template/{template}").or(HEAD("/beat/_template/{template}"));
        HandlerFunction<ServerResponse> handler = metricbeatMetricImporter()::headBeatTemplate;
        return route(predicate, handler);
    }

    @Bean
    public RouterFunction<ServerResponse> metricbeat_importer_bulk_router() {
        RequestPredicate predicate = POST("/beat/_bulk");
        HandlerFunction<ServerResponse> handler = metricbeatMetricImporter()::handle;
        return route(predicate, handler);
    }

    @Bean
    public MetricbeatMetricImporter metricbeatMetricImporter() {
        return new MetricbeatMetricImporter();
    }

}
