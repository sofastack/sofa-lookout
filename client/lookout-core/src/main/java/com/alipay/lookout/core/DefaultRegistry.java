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
package com.alipay.lookout.core;

import com.alipay.lookout.api.*;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;

/**
 * Default implementation of registry.
 */
public class DefaultRegistry extends AbstractRegistry {

    public DefaultRegistry() {
        this(Clock.SYSTEM);
    }

    public DefaultRegistry(Clock clock) {
        this(clock, new LookoutConfig());
    }

    public DefaultRegistry(MetricConfig metricConfig) {
        this(Clock.SYSTEM, metricConfig);
    }

    public DefaultRegistry(Clock clock, MetricConfig metricConfig) {
        super(clock, metricConfig);
    }

    @Override
    protected Counter newCounter(Id id) {
        return new DefaultCounter(clock(), id);
    }

    @Override
    protected DistributionSummary newDistributionSummary(Id id) {
        DefaultDistributionSummary distributionSummary = new DefaultDistributionSummary(clock(), id);
        return distributionSummary;
    }

    @Override
    protected Timer newTimer(Id id) {
        return new DefaultTimer(clock(), id);
    }

    @Override
    protected Metric newMixinMetric(Id id) {
        return new DefaultMixinMetric(id, new DefaultRegistry(clock()));
    }

}
