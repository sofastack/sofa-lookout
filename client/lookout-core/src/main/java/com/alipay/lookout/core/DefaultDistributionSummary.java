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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Distribution summary implementation for the default registry.
 */
final class DefaultDistributionSummary implements DistributionSummary {

    private final Clock      clock;
    private final Id         id;
    private final AtomicLong count;
    private final AtomicLong totalAmount;

    DefaultDistributionSummary(Clock clock, Id id) {
        this.clock = clock;
        this.id = id;
        count = new AtomicLong(0L);
        totalAmount = new AtomicLong(0L);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public void record(long amount) {
        if (amount >= 0) {
            totalAmount.addAndGet(amount);
            count.incrementAndGet();
        }
    }

    @Override
    public Indicator measure() {
        long now = clock.wallTime();
        return new Indicator(now, id).addMeasurement(Statistic.count.name(), count.get())
            .addMeasurement(Statistic.totalAmount.name(), totalAmount.get());
    }

    @Override
    public long count() {
        return count.get();
    }

    @Override
    public long totalAmount() {
        return totalAmount.get();
    }
}
