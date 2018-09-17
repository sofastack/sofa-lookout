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

import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.remote.step.LookoutRegistry;

import static com.alipay.lookout.core.config.LookoutConfig.APP_NAME;
import static com.alipay.lookout.core.config.LookoutConfig.LOOKOUT_EXPORTER_ENABLE;

/**
 * Created by kevin.luy@alipay.com on 2017/10/4.
 */
public class DefaultLookoutClient extends AbstractLookoutClient {
    private volatile boolean isAutoRegisterExtendedMetrics = false;

    public DefaultLookoutClient(String appName) {
        super(appName);
        try {
            Lookout.setRegistry(getInnerCompositeRegistry());
        } catch (IllegalStateException e) {
            logger.warn("global registry is already set!" + e.getMessage());
        }
    }

    @Override
    public synchronized void addRegistry(MetricRegistry registry) {
        if (registry == null) {
            return;
        }
        if (isAutoRegisterExtendedMetrics) {
            registry.registerExtendedMetrics();
        }
        super.addRegistry(registry);

        if (registry instanceof LookoutRegistry) {
            MetricConfig config = ((LookoutRegistry) registry).getConfig();
            if (!config.containsKey(APP_NAME)) {
                //HttpObserver needs
                config.setProperty(APP_NAME, getAppName());
            }
            if (!config.getBoolean(LOOKOUT_EXPORTER_ENABLE, false)) {
                return;
            }
            try {
                setMetricsHttpExporter(PollerUtils.exportHttp((LookoutRegistry) registry));
            } catch (Exception e) {
                logger.error("fail to start MetricsHttpExporter", e);
            }
        }
    }

    /**
     * register extended metrics for all registries in this client
     */
    @Override
    public synchronized void registerExtendedMetrics() {
        isAutoRegisterExtendedMetrics = true;
        //对已有registry补偿登记
        super.registerExtendedMetrics();
    }
}
