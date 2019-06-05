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

import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.ManualClock;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.core.DefaultRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/31
 */
public class LookoutTimerTest {
    @Test
    public void test() {
        final ManualClock clock = new ManualClock();
        DefaultRegistry registry = new DefaultRegistry(clock);
        LookoutTimer timer = new LookoutTimer(registry.createId("timer"), clock, 10L);
        // 原来的step是10
        // 可以修改成20L
        timer.setStep(20L);

        timer.record(1, TimeUnit.MILLISECONDS);

        clock.setWallTime(10L);
        // 如果没有setStep(20) 那么这里应该是1L
        assertThat(timer.count()).isEqualTo(0L);

        // 因为设置了20L 所以这里才是1L
        clock.setWallTime(20L);
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    public void test_normal() throws Exception {
        final ManualClock clock = new ManualClock();
        DefaultRegistry registry = new DefaultRegistry(clock);
        LookoutTimer timer = new LookoutTimer(registry.createId("timer"), clock, 10L);

        assertThat(timer.count()).isEqualTo(0L);
        assertThat(timer.totalTime()).isEqualTo(0L);

        timer.record(10, TimeUnit.MILLISECONDS);

        assertThat(timer.count()).isEqualTo(0L);
        assertThat(timer.totalTime()).isEqualTo(0L);

        clock.setWallTime(10L);

        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime()).isEqualTo(10000000L);

        timer.record(new Runnable() {
            @Override
            public void run() {
                clock.setWallTime(14L);
            }
        });
        timer.record(new Callable<Void>() {
            @Override
            public Void call() {
                clock.setWallTime(16L);
                return null;
            }
        });

        clock.setWallTime(20L);
        assertThat(timer.count()).isEqualTo(2L);
        assertThat(timer.totalTime()).isEqualTo(6000000L);
    }

    @Test
    public void testBuckets() {
        final ManualClock clock = new ManualClock();
        DefaultRegistry registry = new DefaultRegistry(clock);
        LookoutTimer timer = new LookoutTimer(registry.createId("timer"), clock, 10L);
        timer.buckets(new long[] { 100, 200, 300, 400, 500 });
        for (int i = 1; i <= 1000; i++) {
            timer.record(i, TimeUnit.MILLISECONDS);
        }
        clock.setWallTime(0l);
        long sum = 0;
        for (Metric metric : timer) {
            Indicator indicator = metric.measure();
            Collection<Measurement<Long>> measurements = indicator.measurements();
            for (Measurement<Long> measurement : measurements) {
                sum += measurement.value();
            }
        }
        Assert.assertEquals(0, sum);
        clock.setWallTime(10l);
        sum = 0;
        for (Metric metric : timer) {
            Indicator indicator = metric.measure();
            Collection<Measurement<Long>> measurements = indicator.measurements();
            for (Measurement<Long> measurement : measurements) {
                sum += measurement.value();
            }
        }
        Assert.assertEquals(1000, sum);
    }
}