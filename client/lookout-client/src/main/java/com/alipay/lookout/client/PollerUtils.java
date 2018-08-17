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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.report.poller.Listener;
import com.alipay.lookout.remote.report.poller.MetricsHttpExporter;
import com.alipay.lookout.remote.report.poller.PollerController;
import com.alipay.lookout.remote.report.poller.ResettableStepRegistry;
import com.alipay.lookout.remote.step.LookoutRegistry;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiangfeng.xzc
 * @date 2018/8/16
 */
final class PollerUtils {
    private static final Logger LOGGER = LookoutLoggerFactory.getLogger(PollerUtils.class);

    private PollerUtils() {
    }

    /**
     * 辅助方法, 通过HTTP暴露自身的metrics数据
     *
     * @param config
     * @param client
     * @return
     * @throws Exception
     */
    static MetricsHttpExporter exportHttp(LookoutConfig config, AbstractLookoutClient client)
                                                                                             throws Exception {
        ResettableStepRegistry resettableStepRegistry = new ResettableStepRegistry(Clock.SYSTEM,
            config);

        final List<LookoutRegistry> lookoutRegistryList = new ArrayList<LookoutRegistry>();
        for (Registry r : client.getInnerCompositeRegistry().getRegistries()) {
            if (r instanceof LookoutRegistry) {
                lookoutRegistryList.add((LookoutRegistry) r);
            }
        }
        PollerController controller = new PollerController(resettableStepRegistry);
        controller.addListener(new Listener() {
            @Override
            public void onActive() {
                for (LookoutRegistry r : lookoutRegistryList) {
                    r.getMetricObserverComposite().setEnabled(false);
                }
            }

            @Override
            public void onIdle() {
                for (LookoutRegistry r : lookoutRegistryList) {
                    r.getMetricObserverComposite().setEnabled(false);
                }
            }
        });
        try {
            MetricsHttpExporter exporter = new MetricsHttpExporter(controller);
            exporter.start();
            return exporter;
        } catch (Exception e) {
            try {
                controller.close();
            } catch (Exception e2) {
                LOGGER.error("fail to close controller", e2);
            }
            throw e;
        }
    }
}
