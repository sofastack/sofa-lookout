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
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.spi.MetricsImporter;
import com.alipay.lookout.spi.MetricsImporterLocator;
import org.slf4j.Logger;

import java.util.Iterator;

import static com.alipay.lookout.core.config.LookoutConfig.LOOKOUT_MAX_METRICS_NUMBER;
import static com.alipay.lookout.dropwizard.metrics.NameUtils.toMetricName;

/**
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
public class DropWizardMetricsRegistry extends MetricRegistry {
    protected final Logger                            logger        = LookoutLoggerFactory
                                                                        .getLogger(DropWizardMetricsRegistry.class);

    private final com.codahale.metrics.MetricRegistry impl;

    private final MetricConfig                        config;
    private volatile boolean                          maxNumWarning = true;

    /**
     * Create a new instance.
     */
    public DropWizardMetricsRegistry() {
        this(Clock.SYSTEM, new com.codahale.metrics.MetricRegistry());
    }

    /**
     * Create a new instance.
     */
    public DropWizardMetricsRegistry(Clock clock, com.codahale.metrics.MetricRegistry impl) {
        this(clock, impl, new LookoutConfig());
    }

    public DropWizardMetricsRegistry(Clock clock, com.codahale.metrics.MetricRegistry impl,
                                     MetricConfig metricConfig) {
        super(clock);
        this.impl = impl;
        this.config = metricConfig;
    }

    @Override
    public <T extends Number> Gauge<T> gauge(Id id, Gauge<T> gauge) {
        if (overSize(id)) {
            return NoopRegistry.INSTANCE.gauge(id, gauge);
        }
        final String name = toMetricName(id);
        impl.register(name, new DWMetricsGuage(gauge));
        return gauge;
    }

    @Override
    public Counter counter(Id id) {
        if (overSize(id)) {
            return NoopRegistry.INSTANCE.counter(id);
        }
        final String name = toMetricName(id);
        return new DwMetricsCounter(clock(), id, impl.counter(name));
    }

    @Override
    public DistributionSummary distributionSummary(Id id) {
        if (overSize(id)) {
            return NoopRegistry.INSTANCE.distributionSummary(id);
        }
        final String name = toMetricName(id);
        return new DwMetricsDistributionSummary(clock(), id, impl.histogram(name));
    }

    @Override
    public Timer timer(Id id) {
        if (overSize(id)) {
            return NoopRegistry.INSTANCE.timer(id);
        }
        final String name = toMetricName(id);
        return new DwMetricsTimer(clock(), id, impl.timer(name));
    }

    @Override
    public void register(final Metric metric) {
        final String name = toMetricName(metric.id());
        if (metric instanceof Gauge) {
            impl.register(name, new com.codahale.metrics.Gauge() {
                @Override
                public Object getValue() {
                    return ((Gauge) metric).value();
                }
            });
        }
        throw new IllegalArgumentException("Unknown metric to convert to dropwizard metric!"
                                           + metric);
    }

    @Override
    public void removeMetric(Id id) {
        final String name = toMetricName(id);
        impl.remove(name);
    }

    @Override
    public void propagate(String msg, Throwable t) {
        super.propagate(msg, t);
    }

    @Override
    public MixinMetric mixinMetric(Id id) {
        return new DwMetricsMixin(id, this);
    }

    @Override
    public void registerExtendedMetrics() {
        for (MetricsImporter metricsImporter : MetricsImporterLocator.locate()) {
            metricsImporter.register(this);
        }
    }

    @Override
    public <I, Y extends Info<I>> Info info(Id id, Y info) {
        return null;
    }

    @Override
    public <X extends Metric> X get(Id id) {
        return null;
    }

    @Override
    public Iterator<Metric> iterator() {
        return null;
    }

    public boolean overSize(Id id) {
        if (impl.getMetrics().size() >= config.getInt(LOOKOUT_MAX_METRICS_NUMBER,
            MetricConfig.DEFAULT_MAX_METRICS_NUM)) {
            if (maxNumWarning) {
                logger
                    .warn(
                        "metrics number reach max limit: {}! Do not record this new metric(id:{}).",
                        config.getInt(LOOKOUT_MAX_METRICS_NUMBER,
                            MetricConfig.DEFAULT_MAX_METRICS_NUM), id);
                maxNumWarning = false;
            }
            return true;
        }
        return false;
    }

}
