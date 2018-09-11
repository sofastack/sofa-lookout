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

import org.junit.Assert;
import org.junit.Test;

public class DefaultDistributionSummaryTest {

    private final ManualClock clock = new ManualClock();

    @Test
    public void testInit() {
        DistributionSummary t = new DefaultDistributionSummary(clock, NoopId.INSTANCE);
        Assert.assertEquals(t.count(), 0L);
        Assert.assertEquals(t.totalAmount(), 0L);
    }

    @Test
    public void testRecord() {
        DistributionSummary t = new DefaultDistributionSummary(clock, NoopId.INSTANCE);
        t.record(42);
        Assert.assertEquals(t.count(), 1L);
        Assert.assertEquals(t.totalAmount(), 42L);
    }

    @Test
    public void testRecordNegative() {
        DistributionSummary t = new DefaultDistributionSummary(clock, NoopId.INSTANCE);
        t.record(-42);
        Assert.assertEquals(t.count(), 0L);
        Assert.assertEquals(t.totalAmount(), 0L);
    }

    @Test
    public void testRecordZero() {
        DistributionSummary t = new DefaultDistributionSummary(clock, NoopId.INSTANCE);
        t.record(0);
        Assert.assertEquals(t.count(), 1L);
        Assert.assertEquals(t.totalAmount(), 0L);
    }

    @Test
    public void testRecordBuckets() {
        DefaultRegistry registry = new DefaultRegistry();
        Id id = registry.createId("test").withTag("a", "1");
        DistributionSummary t = registry.distributionSummary(id);
        long[] buckets = new long[] { 1, 2, 4, 8 };
        t.buckets(buckets);
        for (int i = 1; i <= 16; i++) {
            t.record(i);
        }
        long sum = 0;
        for (Metric metric : registry) {
            if (metric.id().name().endsWith(".buckets")) {
                for (Object o : metric.measure().measurements()) {
                    Measurement<Long> m = (Measurement<Long>) o;
                    sum += m.value();
                }
            }
        }
        Assert.assertEquals(16, sum);
    }

}
