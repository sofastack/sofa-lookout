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

    private final Clock        clock;
    private final Id           id;
    private final AtomicLong   count;
    private final AtomicLong   totalAmount;
    private       long[]       buckets;
    private       AtomicLong[] cumulativeCounts;

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
            if (buckets != null) {
                recordBucket(amount);
            }
        }
    }

    private void recordBucket(long amount) {
        int i = 0;
        for (; i < buckets.length; ++i) {
            if (amount < buckets[i]) {
                break;
            }
        }
        if (cumulativeCounts[i] == null) {
            cumulativeCounts[i] = new AtomicLong(0);
        }
        cumulativeCounts[i].incrementAndGet();
    }

    @Override
    public Indicator measure() {
        long now = clock.wallTime();
        Indicator indicator = new Indicator(now, id).addMeasurement(Statistic.count.name(), count.get())
                .addMeasurement(Statistic.totalAmount.name(), totalAmount.get());
        for (int i = 0; i < cumulativeCounts.length; i++) {
            if (cumulativeCounts[i] != null) {
                String bucketTag = i < buckets.length ? String.valueOf(buckets[i - 1]) : INFINITY;
                indicator.addMeasurement(bucketTag, cumulativeCounts[i].get());
            }
        }
        return indicator;
    }

    @Override
    public long count() {
        return count.get();
    }

    @Override
    public long totalAmount() {
        return totalAmount.get();
    }

    @Override
    public void enableBuckets(long[] buckets) {
        this.buckets = buckets;
        this.cumulativeCounts = new AtomicLong[buckets.length + 1];
    }

}
