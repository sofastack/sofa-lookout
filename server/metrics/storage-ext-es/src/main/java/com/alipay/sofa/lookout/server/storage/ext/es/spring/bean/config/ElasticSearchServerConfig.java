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
package com.alipay.sofa.lookout.server.storage.ext.es.spring.bean.config;

import com.alipay.sofa.lookout.server.prom.ql.engine.PromQLEngine;
import com.alipay.sofa.lookout.server.prom.storage.Storage;
import com.alipay.sofa.lookout.server.storage.ext.es.ElasticSearchStorage;
import com.alipay.sofa.lookout.server.storage.ext.es.operation.ESMetricsDataOperator;
import io.searchbox.client.JestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by kevin.luy@alipay.com on 2018/3/16.
 */
@ConditionalOnProperty(prefix = "metrics.server", name = "storage", havingValue = "es")
@Configuration
public class ElasticSearchServerConfig {
    @Value("${metrics.es.index:metrics}")
    private String  index;

    @Value("${scheduler.enabled:true}")
    private boolean rolloverIndex = true;

    @Bean
    public PromQLEngine engine(JestClient jestClient) {
        return new PromQLEngine(storage(jestClient));
    }

    @Bean
    public Storage storage(JestClient jestClient) {
        ElasticSearchStorage esStorage = new ElasticSearchStorage(jestClient, index);
        esStorage.setHealthCheck(true);
        return esStorage;
    }

    @ConditionalOnProperty(prefix = "metrics.server.es.operation", name = "auto", havingValue = "true", matchIfMissing = true)
    @Bean
    public ESMetricsDataOperator esOperator(Storage storage) {
        return new ESMetricsDataOperator((ElasticSearchStorage) storage);
    }
}
