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
package com.alipay.sofa.lookout.gateway.metrics.starter;

import com.alipay.sofa.lookout.gateway.core.prototype.exporter.ExportChainManager;
import com.alipay.sofa.lookout.gateway.core.prototype.pipeline.NoInputProcessor;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
public class SelfMetricsStarter {

    @Autowired
    NoInputProcessor<Void, Metric> selfImporterProcessor;

    @Autowired
    PostFilterManager              metricPostFilter;

    @Autowired
    ExportChainManager<Metric>     exportChainManager;

    @PostConstruct
    public void init() {
        selfImporterProcessor.filter(metricPostFilter.asPredicate(null))
            .consume(exportChainManager::export);
    }
}
