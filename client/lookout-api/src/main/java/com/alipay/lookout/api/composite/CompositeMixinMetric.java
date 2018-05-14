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
package com.alipay.lookout.api.composite;

import com.alipay.lookout.api.*;

import java.util.Collection;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
class CompositeMixinMetric extends CompositeMetric implements MixinMetric {

    private final Clock clock;

    public CompositeMixinMetric(Id id, Clock clock, Collection<Registry> registries) {
        super(id, registries);
        this.clock = clock;
    }

    @Override
    public Counter counter(String componentCounterName) {
        return new CompositeComponentCounter(id, registries, componentCounterName);
    }

    @Override
    public Timer timer(String componentTimerName) {
        return new CompositeComponentTimer(id, clock, registries, componentTimerName);
    }

    @Override
    public DistributionSummary distributionSummary(String componentDistributionSummaryName) {
        return new CompositeComponentDistributionSummary(id, registries,
            componentDistributionSummaryName);
    }

    @Override
    public <T extends Number> Gauge<T> gauge(String componentGaugeName, Gauge<T> componentGauge) {
        Gauge<T> old = null;
        for (Registry registry : registries) {
            MixinMetric mixinMetric = registry.mixinMetric(id);
            if (mixinMetric != null) {
                Gauge<T> oldOne = mixinMetric.gauge(componentGaugeName, componentGauge);
                if (old == null && oldOne != null) {
                    old = oldOne;//choose first one.
                }
            }
        }
        return old;
    }

    @Override
    protected <T extends Metric> T getMetric(Registry registry) {
        throw new UnsupportedOperationException();
    }

    private MixinMetric getMixinMetric(Registry registry) {
        Metric metric = registry.mixinMetric(id());
        //reach the metrics max number,metric will be null;
        return metric == null ? NoopMixinMetric.INSTANCE : (MixinMetric) metric;
    }

    class CompositeComponentCounter extends CompositeCounter {
        private String componentMetricName;

        public CompositeComponentCounter(Id id, Collection<Registry> registries,
                                         String componentMetricName) {
            super(id, registries);
            this.componentMetricName = componentMetricName;
        }

        @Override
        protected Counter getMetric(Registry registry) {
            MixinMetric mixinMetric = getMixinMetric(registry);
            return mixinMetric.counter(componentMetricName);
        }

    }

    class CompositeComponentDistributionSummary extends CompositeDistributionSummary {
        private String componentMetricName;

        public CompositeComponentDistributionSummary(Id id, Collection<Registry> registries,
                                                     String componentMetricName) {
            super(id, registries);
            this.componentMetricName = componentMetricName;
        }

        @Override
        protected DistributionSummary getMetric(Registry registry) {
            MixinMetric mixinMetric = getMixinMetric(registry);
            return mixinMetric.distributionSummary(componentMetricName);
        }
    }

    class CompositeComponentTimer extends CompositeTimer {
        private String componentMetricName;

        public CompositeComponentTimer(Id id, Clock clock, Collection<Registry> registries,
                                       String componentMetricName) {
            super(id, clock, registries);
            this.componentMetricName = componentMetricName;
        }

        @Override
        protected Timer getMetric(Registry registry) {
            MixinMetric mixinMetric = getMixinMetric(registry);
            return mixinMetric.timer(componentMetricName);
        }
    }

}
