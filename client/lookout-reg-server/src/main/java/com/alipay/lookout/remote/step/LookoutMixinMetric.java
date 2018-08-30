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
package com.alipay.lookout.remote.step;

import com.alipay.lookout.api.ResettableStep;
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.DistributionSummary;
import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.Timer;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.core.common.MeasurementUtil;

import static com.alipay.lookout.common.LookoutConstants.DOT;

/**
 * Created by kevin.luy@alipay.com on 2017/3/26.
 */
final class LookoutMixinMetric implements MixinMetric, ResettableStep {
    private final Id           id;
    private final StepRegistry registry; //inner registry with fixed StepMillis
    private final StepClock    stepClock;

    public LookoutMixinMetric(Id id, StepRegistry registry, StepClock stepClock) {
        this.id = id;
        this.registry = registry;
        this.stepClock = stepClock;
    }

    @Override
    public void setStep(long step) {
        registry.setStep(step);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Counter counter(String componentCounterName) {
        return registry.counter(registry.createId(componentCounterName));
    }

    @Override
    public Timer timer(String componentTimerName) {
        return registry.timer(registry.createId(componentTimerName));
    }

    @Override
    public DistributionSummary distributionSummary(String componentDistributionSummaryName) {
        return registry.distributionSummary(registry.createId(componentDistributionSummaryName));
    }

    @Override
    public <T extends Number> Gauge<T> gauge(final String componentGaugeName,
                                             final Gauge<T> componentGauge) {
        return registry.gauge(registry.createId(componentGaugeName), componentGauge);
    }

    @Override
    public Indicator measure() {
        Indicator indicator = new Indicator(stepClock.wallTime(), id);
        for (Metric metricEntry : registry) {
            Indicator tmp = metricEntry.measure();
            for (Object measureObj : tmp.measurements()) {
                Measurement measure = (Measurement) measureObj;
                //add componentName to measureName;
                String measureName = measure.name();
                if (!MeasurementUtil.isEmptyMeasureName(measure.name())) {
                    measureName = metricEntry.id().name() + DOT + measureName;
                } else {
                    measureName = metricEntry.id().name();
                }
                indicator.addMeasurement(new Measurement(measureName, measure.value()));
            }
        }
        return indicator;
    }
}
