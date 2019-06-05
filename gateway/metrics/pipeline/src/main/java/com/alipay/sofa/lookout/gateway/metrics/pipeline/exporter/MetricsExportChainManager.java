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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.exporter;

import com.alipay.sofa.lookout.gateway.core.common.DataType;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.ExportChainManager;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.chain.ExportChain;
import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycleSupport;
import com.alipay.sofa.lookout.gateway.core.utils.ListUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 需要能够根据ops拉到的配置修改dynamicChains
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class MetricsExportChainManager extends LifeCycleSupport implements
                                                               ExportChainManager<Metric> {
    private static final Logger                    LOGGER    = LoggerFactory
                                                                 .getLogger(MetricsExportChainManager.class);
    private final List<ExportChain<Metric>>        metricChains;
    private final List<ExportChain<Metric>>        infoChains;
    private volatile List<ExportChain<Metric>>     dynamicChains;
    private final List<DynamicExportChainProvider> providers;

    /**
     * 内部刷新 dynamicChains 的调度线程池
     */
    private final ScheduledExecutorService         scheduler = Executors
                                                                 .newSingleThreadScheduledExecutor();

    public MetricsExportChainManager(List<ExportChain<Metric>> staticChains, List<DynamicExportChainProvider> providers) {
        List<ExportChain<Metric>> infoChains;
        if (staticChains == null) {
            LOGGER.error("没有检测到静态export chain");
            infoChains = staticChains = Collections.emptyList();
        } else {
            infoChains = staticChains.stream()
                    .filter(x -> x.exporter().supports(DataType.METRIC_INFO))
                    .collect(Collectors.toList());

            staticChains = staticChains.stream()
                    .filter(x -> x.exporter().supports(DataType.METRIC))
                    .collect(Collectors.toList());
        }

        this.metricChains = ListUtils.unmodifiableList(staticChains);
        this.infoChains = ListUtils.unmodifiableList(infoChains);
        this.dynamicChains = Collections.emptyList();
        this.providers = ListUtils.unmodifiableList(providers);
    }

    @Override
    protected void doStart() {
        super.doStart();
        this.refresh();
        // 每分钟刷新一次
        scheduler.scheduleWithFixedDelay(this::refresh, 1, 1, TimeUnit.MINUTES);
    }

    /**
     */
    public void refresh() {
        // 旧的备份一下, 用于失败的时候还原???
        // List<ExportChain<Metric>> oldDynamicChains = this.dynamicChains;

        List<ExportChain<Metric>> dynamicChains = new ArrayList<>();
        for (DynamicExportChainProvider provider : providers) {
            try {
                List<ExportChain<Metric>> chains = provider.get();
                if (chains != null) {
                    dynamicChains.addAll(chains);
                }
            } catch (Exception e) {
                LOGGER.error("fail to call DynamicExportChainProvider {}", provider, e);
            }
        }

        // 替换引用
        this.dynamicChains = dynamicChains;
    }

    @Override
    public void export(Metric m) {
        // 是一个metrics
        if (m.getInfo() == null) {
            for (ExportChain<Metric> chain : metricChains) {
                accept(chain, m);
            }
            for (ExportChain<Metric> chain : dynamicChains) {
                accept(chain, m);
            }
        } else {
            // 是一个info
            for (ExportChain<Metric> chain : infoChains) {
                accept(chain, m);
            }
        }
    }

    private void accept(ExportChain<Metric> chain, Metric m) {
        try {
            chain.accept(m);
        } catch (Exception e) {
            LOGGER.error("chain.accept 错误", e);
        }
    }
}
