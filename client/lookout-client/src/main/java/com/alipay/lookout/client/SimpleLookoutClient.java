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
import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.core.AbstractRegistry;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.remote.report.xflush.Listener;
import com.alipay.lookout.remote.report.xflush.XFlushHttpExporter;
import com.alipay.lookout.remote.step.PollerController;
import com.alipay.lookout.remote.step.SettableStepRegistry;
import com.alipay.lookout.remote.step.LookoutRegistry;

import java.io.IOException;
import java.util.List;
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
    private final LookoutConfig lookoutConfig;

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
            : new MetricRegistry[]{new LookoutRegistry(lookoutConfig)};

        lookoutConfig.setProperty(LookoutConfig.APP_NAME, appName);
        if (!lookoutConfig.getBoolean(LookoutConfig.LOOKOUT_ENABLE, true)) {
            return;
        }

        // final List<LookoutRegistry> lookoutRegistryList = new ArrayList<LookoutRegistry>();

        for (MetricRegistry registry : registries) {
            // if (registry instanceof LookoutRegistry) {
            //    lookoutRegistryList.add((LookoutRegistry) registry);
            // }
            if (registry instanceof AbstractRegistry
                && ((AbstractRegistry) registry).getConfig() != lookoutConfig) {
                // reset with the same configuration
                ((AbstractRegistry) registry).setConfig(lookoutConfig);
            }
            //add jvm and other metrics
            registry.registerExtendedMetrics();
            super.addRegistry(registry);
        }

        // if (lookoutConfig.getBoolean(LookoutConfig.XFLUSH_EXPORTER_ENABLE, true)) {
        //     registerFooRegistry(lookoutRegistryList);
        // }


        logger.debug("set global registry to Lookout");
        // init global registry
        Lookout.setRegistry(getRegistry());
    }

    private void registerFooRegistry(final List<LookoutRegistry> lookoutRegistryList) {
        SettableStepRegistry settableStepRegistry = new SettableStepRegistry(Clock.SYSTEM, lookoutConfig);
        settableStepRegistry.registerExtendedMetrics();
        PollerController controller = new PollerController(settableStepRegistry);
        XFlushHttpExporter exporter = new XFlushHttpExporter(controller, lookoutConfig);
        // 如果exporter处于激活状态就禁止lookout的自动上报
        exporter.addListener(new Listener() {
            @Override
            public void onActive() {
                for (LookoutRegistry lookoutRegistry : lookoutRegistryList) {
                    lookoutRegistry.getMetricObserverComposite().setEnabled(false);
                }
            }

            @Override
            public void onIdle() {
                for (LookoutRegistry lookoutRegistry : lookoutRegistryList) {
                    lookoutRegistry.getMetricObserverComposite().setEnabled(true);
                }
            }
        });

        try {
            exporter.start();
            super.addRegistry(settableStepRegistry);
        } catch (IOException e) {
            logger.error("fail to start XFlushHttpExporter", e);
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
