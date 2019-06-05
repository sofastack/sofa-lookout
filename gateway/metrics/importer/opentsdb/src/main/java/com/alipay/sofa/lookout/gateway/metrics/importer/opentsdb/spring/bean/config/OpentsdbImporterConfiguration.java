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
package com.alipay.sofa.lookout.gateway.metrics.importer.opentsdb.spring.bean.config;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.ConditionalOnImporterComponent;
import com.alipay.sofa.lookout.gateway.metrics.importer.opentsdb.OpentsdbMetricImporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/26
 */
@ConditionalOnImporterComponent(value = "opentsdb", type = MonitorComponent.METRIC)
@Configuration
public class OpentsdbImporterConfiguration {
    @Bean
    RouterFunction<ServerResponse> opentsdbMetricImporter_router() {
        RequestPredicate predicate = POST("/opentsdb/api/put");
        HandlerFunction<ServerResponse> handler = opentsdbMetricImporter()::handle;
        return RouterFunctions.route(predicate, handler);
    }

    @Bean
    public OpentsdbMetricImporter opentsdbMetricImporter() {
        return new OpentsdbMetricImporter();
    }
}
