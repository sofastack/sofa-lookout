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

/**
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;

/**
 * Counter implementation for the metric3 registry.
 */
class DwMetricsCounter implements Counter, DwMetricWrapper<com.codahale.metrics.Counter> {

    private final Clock                        clock;
    private final Id                           id;
    private final com.codahale.metrics.Counter impl;

    DwMetricsCounter(Clock clock, Id id, com.codahale.metrics.Counter impl) {
        this.clock = clock;
        this.id = id;
        this.impl = impl;
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Indicator measure() {
        long now = clock.wallTime();
        long v = impl.getCount();
        return new Indicator(now, id, v);
    }

    @Override
    public void inc() {
        impl.inc();
    }

    @Override
    public void inc(long amount) {
        impl.inc(amount);
    }

    @Override
    public void dec() {
        impl.dec();
    }

    @Override
    public void dec(long n) {
        impl.dec(n);
    }

    @Override
    public long count() {
        return impl.getCount();
    }

    @Override
    public com.codahale.metrics.Counter getOriginDwMetric() {
        return impl;
    }
}
