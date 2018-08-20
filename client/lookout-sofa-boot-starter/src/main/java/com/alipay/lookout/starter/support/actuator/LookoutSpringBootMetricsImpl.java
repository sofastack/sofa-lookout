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
package com.alipay.lookout.starter.support.actuator;

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LookoutMetricServicesImpl
 *
 * @author yangguanchao
 * @since 2018/06/04
 */
public class LookoutSpringBootMetricsImpl implements CounterService, GaugeService {

    public static final String                              LOOKOUT_COUNTER_PREFIX = "counter.";

    public static final String                              LOOKOUT_GAUGE_PREFIX   = "gauge.";

    private final Registry                                  registry;

    private final ConcurrentMap<String, SimpleLookoutGauge> gauges                 = new ConcurrentHashMap<String, SimpleLookoutGauge>();

    public LookoutSpringBootMetricsImpl(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void increment(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        Id id = this.registry.createId(metricName);
        Counter counter = this.registry.counter(id);
        counter.inc();
    }

    @Override
    public void decrement(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        Id id = this.registry.createId(metricName);
        Counter counter = this.registry.counter(id);
        counter.dec();
    }

    @Override
    public void reset(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        Id id = this.registry.createId(metricName);
        this.registry.removeMetric(id);
    }

    @Override
    public void submit(String metricName, double value) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        metricName = wrapName(LOOKOUT_GAUGE_PREFIX, metricName);
        SimpleLookoutGauge gauge = this.gauges.get(metricName);
        if (gauge == null) {
            SimpleLookoutGauge newGauge = new SimpleLookoutGauge(value);
            gauge = this.gauges.putIfAbsent(metricName, newGauge);
            if (gauge == null) {
                Id id = this.registry.createId(metricName);
                this.registry.gauge(id, newGauge);
                return;
            }
        }
        gauge.setValue(value);
    }

    private String wrapName(String prefix, String metricName) {
        if (StringUtils.isBlank(metricName)) {
            throw new RuntimeException("Metric name can't be blank!");
        }
        if (metricName.startsWith(prefix)) {
            return metricName;
        }
        return prefix + metricName;
    }

    private final static class SimpleLookoutGauge implements Gauge<Double> {

        private volatile double value;

        private SimpleLookoutGauge(double value) {
            this.value = value;
        }

        @Override
        public Double value() {
            return this.value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

}
