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
package com.alipay.sofa.lookout.gateway.metrics.importer.standard.spring.bean.config;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.ConditionalOnImporterComponent;
import com.alipay.sofa.lookout.gateway.metrics.importer.standard.StandardMetricImporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/26
 */
@ConditionalOnImporterComponent(value = "standard", type = MonitorComponent.METRIC)
@Configuration
public class StandardImporterConfiguration {

    /**
     * lookout客户端的上报入口
     *
     * @return
     */
    @Bean
    RouterFunction<ServerResponse> standardImporter_router() {
        RequestPredicate predicate = GET("/datas").or(POST("/datas"));
        HandlerFunction<ServerResponse> handler = standardMetricImporter()::handle;
        return RouterFunctions.route(predicate, handler);
    }

    /**
     * 使用自由协议格式上报, 但不使用lookout客户端
     *
     * @return
     */
    @Bean
    RouterFunction<ServerResponse> standardImporterV2_router() {
        RequestPredicate predicate = GET("/lookout/metrics").or(POST("/lookout/metrics/**"));
        // TODO 在原有的handle的逻辑的基础上添加一个标记 用于表示这是一个来自 非lookout客户端的上报请求
        HandlerFunction<ServerResponse> handler = standardMetricImporter()::handle;
        return RouterFunctions.route(predicate, handler);
    }

    @Bean
    public StandardMetricImporter standardMetricImporter() {
        return new StandardMetricImporter();
    }

}
