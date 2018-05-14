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
package com.alipay.lookout.api;

import com.alipay.lookout.api.composite.MixinMetric;

/**
 * Unstable API,Do not use it!
 *
 * Created by kevin.luy@alipay.com on 2017/2/15.
 */
public enum NoopMixinMetric implements MixinMetric {
    INSTANCE;

    @Override
    public Id id() {
        return NoopId.INSTANCE;
    }

    @Override
    public Indicator measure() {
        return new Indicator(-1, id());
    }

    @Override
    public Counter counter(String componentCounterName) {
        return NoopCounter.INSTANCE;
    }

    @Override
    public Timer timer(String componentTimerName) {
        return NoopTimer.INSTANCE;
    }

    @Override
    public DistributionSummary distributionSummary(String componentDistributionSummaryName) {
        return NoopDistributionSummary.INSTANCE;
    }

    @Override
    public <T extends Number> Gauge<T> gauge(String componentGaugeName, Gauge<T> componentGauge) {
        return null;
    }
}
