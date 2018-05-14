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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.core.AbstractTimer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
class DwMetricsTimer extends AbstractTimer implements DwMetricWrapper<com.codahale.metrics.Timer> {

    private final Id                         id;
    private final com.codahale.metrics.Timer impl;
    private final AtomicLong                 totalTime;

    DwMetricsTimer(Clock clock, Id id, com.codahale.metrics.Timer impl) {
        super(clock);
        this.id = id;
        this.impl = impl;
        this.totalTime = new AtomicLong(0L);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public void record(long amount, TimeUnit unit) {
        impl.update(amount, unit);
    }

    @Override
    public Indicator measure() {
        final long now = clock.wallTime();
        return new Indicator(now, id, impl.getMeanRate());
    }

    @Override
    public long count() {
        return impl.getCount();
    }

    @Override
    public long totalTime() {
        return totalTime.get();
    }

    @Override
    public com.codahale.metrics.Timer getOriginDwMetric() {
        return impl;
    }
}
