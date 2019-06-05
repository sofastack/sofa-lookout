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
package com.alipay.sofa.lookout.gateway.metrics.starter.spring.bean.config;

import com.alipay.sofa.lookout.gateway.core.prototype.pipeline.NoInputProcessor;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.SelfReportObserver;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.starter.SelfMetricsStarter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自身埋点的导出结果导出给自己!
 *
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
@Configuration
public class SelfMetricConfiguration {
    @Bean
    NoInputProcessor<Void, Metric> selfImporterProcessor() {
        return new NoInputProcessor<>();
    }

    @Bean
    SelfMetricsStarter selfMetricsStarter() {
        return new SelfMetricsStarter();
    }

    @ConditionalOnMissingBean(SelfReportObserver.class)
    @Bean
    SelfReportObserver selfReportObserver() {
        SelfReportObserver selfReportObserver = new SelfReportObserver();
        selfReportObserver.setConsumer(metric -> {
            selfImporterProcessor().onOutput(metric);
        });
        return selfReportObserver;
    }
}
