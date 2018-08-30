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
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.common.Assert;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.utils.NetworkUtil;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.remote.report.poller.MetricsHttpExporter;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import static com.alipay.lookout.common.LookoutConstants.INSTANCE_ID_NAME;

/**
 * Created by kevin.luy@alipay.com on 2018/4/24.
 */
abstract class AbstractLookoutClient implements LookoutClient {
    Logger                      logger         = LookoutLoggerFactory.getLogger(this.getClass());
    private final String        appName;
    private CompositeRegistry   globalRegistry = new CompositeRegistry(Clock.SYSTEM);
    private MetricsHttpExporter metricsHttpExporter;

    /**
     * new an abstractLookoutClient
     *
     * @param appName application name
     */
    public AbstractLookoutClient(String appName) {
        this.appName = appName;
        Assert.checkArg(StringUtils.isNotEmpty(appName), "appName is required!");
    }

    protected void addRegistry(MetricRegistry registry) {
        Preconditions.checkArgument(!(registry instanceof CompositeRegistry),
            "The registry can not be compositeRegistry!");
        if (registry instanceof CommonTagsAccessor) {
            addDefaultCommonTags((CommonTagsAccessor) registry);
        }
        globalRegistry.add(registry);
        logger.info("add a registry:{}", registry.getClass().getSimpleName());
    }

    protected final CompositeRegistry getInnerCompositeRegistry() {
        return globalRegistry;
    }

    @Override
    public <T extends Registry> T getRegistry() {
        Preconditions.checkState(globalRegistry.getRegistries().size() > 0,
            "No usable metrics registry found!");
        return (T) globalRegistry;
    }

    /**
     * locate all extension module,and import all extended metrics to registry.
     */
    protected void registerExtendedMetrics() {
        logger.debug("register all extended metrics");
        globalRegistry.registerExtendedMetrics();
    }

    /**
     * add default tags,set by force
     *
     * @param commonTagsAccessor commonTagsAccessor
     */
    protected void addDefaultCommonTags(CommonTagsAccessor commonTagsAccessor) {
        logger.debug("add all default common  tags");

        String zone = System.getProperty("com.alipay.ldc.zone");
        if (StringUtils.isNotEmpty(zone)) {
            commonTagsAccessor.setCommonTag("zone", zone);
        }
        //Ant cloud middleware instanceId
        String instanceId = System.getProperty(INSTANCE_ID_NAME);
        if (StringUtils.isNotEmpty(instanceId)) {
            commonTagsAccessor.setCommonTag("instance_id", instanceId);
        }
        commonTagsAccessor.setCommonTag("host", NetworkUtil.getLocalAddress().getHostName());
        commonTagsAccessor.setCommonTag("ip", NetworkUtil.getLocalAddress().getHostAddress());
        commonTagsAccessor.setCommonTag("app", appName);
    }

    @Override
    public void close() throws Exception {
        MetricsHttpExporter metricsHttpExporter = getMetricsHttpExporter();
        if (metricsHttpExporter != null) {
            metricsHttpExporter.close();
        }
    }

    protected MetricsHttpExporter getMetricsHttpExporter() {
        return metricsHttpExporter;
    }

    protected void setMetricsHttpExporter(MetricsHttpExporter metricsHttpExporter) {
        this.metricsHttpExporter = metricsHttpExporter;
    }
}
