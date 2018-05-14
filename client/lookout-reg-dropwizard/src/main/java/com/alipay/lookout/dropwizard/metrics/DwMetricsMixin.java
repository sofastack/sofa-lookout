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
package com.alipay.lookout.dropwizard.metrics;

import com.alipay.lookout.api.*;
import com.alipay.lookout.api.composite.MixinMetric;

/**
 * Created by kevin.luy@alipay.com on 2017/2/15.
 */
final class DwMetricsMixin implements MixinMetric {

    private Id       id;
    private Registry registry;

    public DwMetricsMixin(Id id, Registry registry) {
        this.id = id;
        this.registry = registry;
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Counter counter(String componentCounterName) {
        return registry.counter(createComponentId(id, componentCounterName));
    }

    @Override
    public Timer timer(String componentTimerName) {
        return registry.timer(createComponentId(id, componentTimerName));
    }

    @Override
    public Indicator measure() {
        return null;
    }

    @Override
    public DistributionSummary distributionSummary(String componentDistributionSummaryName) {
        return registry
            .distributionSummary(createComponentId(id, componentDistributionSummaryName));
    }

    @Override
    public <T extends Number> Gauge<T> gauge(String componentGaugeName, Gauge<T> componentGauge) {
        return registry.gauge(createComponentId(id, componentGaugeName), componentGauge);
    }

    private Id createComponentId(Id mixinId, String componentName) {
        return registry.createId(mixinId.name() + "." + componentName).withTags(mixinId.tags());
    }
}
