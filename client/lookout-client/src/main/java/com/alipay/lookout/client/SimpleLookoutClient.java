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
package com.alipay.lookout.client;

import com.alipay.lookout.api.BasicTag;
import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.core.AbstractRegistry;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.remote.report.poller.ResettableStepRegistry;
import com.alipay.lookout.remote.step.LookoutRegistry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 与 @see DefaultLookoutClient 相比，SimpleLookoutClient限制更严格：
 * 1.所有的Registry需要预先设置！
 * 2.所有的Registry使用同一(LookoutConfig)配置实例;(也是推荐的)
 * 3.client实例只能全局唯一！
 * <p>
 * this client instance can be created only once!
 * Created by kevin.luy@alipay.com on 2017/5/23.
 */
public final class SimpleLookoutClient extends AbstractLookoutClient {
    private static final AtomicInteger state = new AtomicInteger(0);
    private final LookoutConfig        lookoutConfig;

    public SimpleLookoutClient(String appName, MetricRegistry... registries) {
        this(appName, null, registries);
    }

    /**
     * all registries will be reset with the same lookout config
     *
     * @param appName    app name
     * @param config     lookout config,
     * @param registries
     */
    public SimpleLookoutClient(String appName, LookoutConfig config, MetricRegistry... registries) {
        super(appName);

        if (!state.compareAndSet(0, 1)) {
            throw new IllegalStateException("support only one lookout client instance now!");
        }
        lookoutConfig = config != null ? config : new LookoutConfig();
        registries = registries.length > 0 ? registries
            : new MetricRegistry[] { new LookoutRegistry(lookoutConfig) };

        lookoutConfig.setProperty(LookoutConfig.APP_NAME, appName);
        if (!lookoutConfig.getBoolean(LookoutConfig.LOOKOUT_ENABLE, true)) {
            return;
        }

        for (MetricRegistry registry : registries) {
            if (registry instanceof AbstractRegistry
                && ((AbstractRegistry) registry).getConfig() != lookoutConfig) {
                // reset with the same configuration
                ((AbstractRegistry) registry).setConfig(lookoutConfig);
            }
            //add jvm and other metrics
            registry.registerExtendedMetrics();
            super.addRegistry(registry);
        }

        exportPoller();

        logger.debug("set global registry to Lookout");
        // init global registry
        Lookout.setRegistry(getRegistry());
    }

    private void exportPoller() {
        if (!lookoutConfig.getBoolean(LookoutConfig.POLLER_EXPORTER_ENABLED, false)) {
            return;
        }
        try {
            ResettableStepRegistry resettableStepRegistry = PollerUtils
                .exportHttp(lookoutConfig, this).getController().getRegistry();
            resettableStepRegistry.registerExtendedMetrics();
            super.addRegistry(resettableStepRegistry);
        } catch (Exception e) {
            logger.error("fail to start MetricsHttpExporter", e);
        }
    }

    /**
     * get the global configuration
     *
     * @return lookout config
     */
    public MetricConfig getLookoutConfig() {
        return lookoutConfig;
    }

    /**
     * add common tags to the registry witch is CommonTagsAccessor.
     *
     * @param basicTag
     */
    public void addCommonTags(BasicTag basicTag) {
        for (Registry r : ((CompositeRegistry) getRegistry()).getRegistries()) {
            if (r instanceof CommonTagsAccessor) {
                ((CommonTagsAccessor) r).setCommonTag(basicTag.key(), basicTag.value());
            }
        }
    }

}
