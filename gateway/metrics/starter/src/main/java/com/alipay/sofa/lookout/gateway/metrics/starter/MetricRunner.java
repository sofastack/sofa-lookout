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
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.PreFilterManager;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.parser.FilterException;
import com.alipay.sofa.lookout.gateway.core.prototype.pipeline.ImporterProcessor;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.importer.AbstractWebfluxImporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.queue.MetricsPersistQueueProcessor;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.reader.ReaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * 编排整个metrics的处理逻辑
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class MetricRunner {
    private static final Logger  LOGGER = LoggerFactory.getLogger(MetricRunner.class);

    // 组件1: 导入器
    @Autowired
    ImporterProcessor<RawMetric> importerProcessor;

    // 组件2: 前置过滤器, TODO 应该具备动态能力
    @Autowired
    PreFilterManager<RawMetric>  metricPreFilter;

    // 组件3: 队列 数据缓冲
    @Autowired
    MetricsPersistQueueProcessor queueProcessor;

    // 组件4: reader负责反序列化
    @Autowired
    ReaderManager                readerManager;

    // 组件5: 后置过滤器, TODO 应该具备动态能力
    @Autowired
    PostFilterManager            metricPostFilter;

    // 组件6: 导出
    @Autowired
    ExportChainManager<Metric>   exportChainManager;

    @Autowired
    ExecutorService              computingThreadPool;

    @PostConstruct
    public void init() {
        //add consumers to importerProcessor.
        importerProcessor.filter(o -> {
            // 通过抛异常的方式, 将错误信息返回给调用方, 否则调用方只能看到消息被接收了, 不知道处理发生了错误 不方便debug
            Filter.FilterResult result = metricPreFilter.test(o, null);
            if (result != Filter.SUCCESS) {
                throw new FilterException(result);
            }
            return true;
        }).then(queueProcessor)
                .observeOn(computingThreadPool)
                .flatMap((RawMetric rm, Consumer<Metric> sink) -> {
                    if (rm.getVersion() == AbstractWebfluxImporter.VERSION) {
                        readerManager.read(rm).forEach(sink);
                    } else {
                        LOGGER.warn("invalid version data {}", rm);
                    }
                })
                .filter(metricPostFilter.asPredicate(null))
                .consume(exportChainManager::export);

        // 启动应该从后向前
        // TODO 临时方案: 手动启动
        queueProcessor.start();
        importerProcessor.start();
    }

    @PreDestroy
    public void stop() {
        importerProcessor.stop();
        queueProcessor.stop();
    }
}
