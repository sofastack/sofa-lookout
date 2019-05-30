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

import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.gateway.core.common.ConditionalOnMonitorComponent;
import com.alipay.sofa.lookout.gateway.core.common.Executors;
import com.alipay.sofa.lookout.gateway.core.common.RefuseRequestService;
import com.alipay.sofa.lookout.gateway.core.common.impl.RefuseRequestServiceImpl;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.ExportChainManager;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.chain.ExportChain;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.PreFilterManager;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.Importer;
import com.alipay.sofa.lookout.gateway.core.prototype.pipeline.ImporterProcessor;
import com.alipay.sofa.lookout.gateway.core.ratelimit.RateLimitService;
import com.alipay.sofa.lookout.gateway.core.ratelimit.impl.UnlimitedRateLimitService;
import com.alipay.sofa.lookout.gateway.core.token.LookoutTokenService;
import com.alipay.sofa.lookout.gateway.core.token.impl.NoopLookoutTokenService;
import com.alipay.sofa.lookout.gateway.core.utils.BlockExecutionHandler;
import com.alipay.sofa.lookout.gateway.metrics.exporter.es.spring.bean.config.EsExporterConfiguration;
import com.alipay.sofa.lookout.gateway.metrics.exporter.standard.spring.bean.config.RelayExporterConfiguration;
import com.alipay.sofa.lookout.gateway.metrics.importer.standard.spring.bean.config.StandardImporterConfiguration;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.exporter.DynamicExportChainProvider;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.exporter.MetricsExportChainManager;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.filter.builtin.post.CommonMetricFilter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.filter.builtin.pre.*;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.queue.MetricsPersistQueueProcessor;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.reader.ReaderManager;
import com.alipay.sofa.lookout.gateway.metrics.starter.spring.bean.config.SelfMetricConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * metrics流水线需要的组件
 * <ul>
 * <li>一组导入器 (目前有多个适配器)</li>
 * <li>一组pre过滤器</li>
 * <li>一个队列处理器</li>
 * <li>一组反序列化reader (需要支持多种类型的反序列化工作)</li>
 * <li>一组post过滤器</li>
 * <li>一组导出器(需要一个管理器来管理)</li>
 * </ul>
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
@ConditionalOnMonitorComponent("metric")
@Configuration
@Import({ SelfMetricConfiguration.class, StandardImporterConfiguration.class,
         RelayExporterConfiguration.class, EsExporterConfiguration.class })
public class MetricPipelineConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricPipelineConfiguration.class);

    @Autowired
    Environment                 env;
    @Autowired
    Registry                    registry;
    @Autowired
    RateLimitService            rateLimitService;
    @Autowired
    LookoutTokenService         lookoutTokenService;

    @Bean
    ImporterProcessor<RawMetric> importerProcessor(@Autowired(required = false) List<Importer<RawMetric>> importers) {
        if (importers == null || importers.isEmpty()) {
            LOGGER.warn("没有检测到任何importer");
        }
        return new ImporterProcessor<>(importers);
    }

    @Bean
    ReaderManager readerManager() {
        return new DefaultReaderManager();
    }

    @Bean
    MetricsPersistQueueProcessor queueProcessor() {
        String dir = System.getProperty("user.home") + "/lookout_gateway_queue_cache";
        String queueName = "queue";
        // TODO 出队的线程有几个? 我感觉也不用太多 因为后续操作是放到计算线程池里的
        int threads = Runtime.getRuntime().availableProcessors();
        return new MetricsPersistQueueProcessor(threads, dir, queueName, registry);
    }

    /**
     * @return
     */
    @Bean(initMethod = "start")
    ExportChainManager<Metric> exportManager(@Autowired(required = false) List<ExportChain<Metric>> staticChains,
                                             @Autowired(required = false) List<DynamicExportChainProvider> providers) {
        return new MetricsExportChainManager(staticChains, providers);
    }

    @ConditionalOnMissingBean(name = "metricPreFilter")
    @Bean
    PreFilterManager<RawMetric> metricPreFilter() {
        // 某些filter需要根据 properties 配置决定是否应该生效
        List<Filter<RawMetric>> filters = Arrays.asList(new MetricDigestPreFilter(),
                new DebugPreFilter(), new TokenPreFilter(lookoutTokenService), new RateLimitPreFilter(
                        lookoutTokenService, rateLimitService), new ImporterStatsFilter(registry,
                        lookoutTokenService));
        return new PreFilterManager<>(filters);
    }

    @ConditionalOnMissingBean(RateLimitService.class)
    @Bean
    RateLimitService rateLimitService() {
        return new UnlimitedRateLimitService();
    }

    @ConditionalOnMissingBean(LookoutTokenService.class)
    @Bean
    LookoutTokenService lookoutTokenService() {
        return new NoopLookoutTokenService();
    }

    @ConditionalOnMissingBean(name = "metricPostFilter")
    @Bean
    PostFilterManager metricPostFilter() {
        List<Filter<Metric>> filters = new ArrayList<>();
        filters.add(new CommonMetricFilter());
        return new PostFilterManager(filters);
    }

    // 计算线程池的大小
    @Value("${metrics.threadpool.computing.size:50}")
    private int computingThreads;

    @Bean
    ExecutorService computingThreadPool() {
        return Executors.newFixedThreadPoolExecutor(computingThreads, 1, "computing",
            new BlockExecutionHandler(), registry);
    }

    @Bean
    MetricRunner metricRunner() {
        return new MetricRunner();
    }

    @Bean
    RefuseRequestService refuseRequestService() {
        return new RefuseRequestServiceImpl();
    }

    @ConditionalOnMissingBean(ExportManageServerRunner.class)
    @Bean
    ExportManageServerRunner exportManageServerRunner() {
        return new ExportManageServerRunner();
    }

}
