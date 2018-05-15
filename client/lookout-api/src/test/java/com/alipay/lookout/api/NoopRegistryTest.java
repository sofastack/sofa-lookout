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
import com.alipay.lookout.api.info.Info;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kevin.luy@alipay.com on 2018/5/16.
 */
public class NoopRegistryTest {
    private Registry r = NoopRegistry.INSTANCE;

    @Test
    public void testNoopCounter() {
        Counter counter = r.counter(r.createId("name"));
        Assert.assertEquals(NoopCounter.INSTANCE, counter);
        counter.inc(8);
        counter.inc();
        counter.dec(1);
        counter.dec();
        Assert.assertEquals(0, counter.count());
    }

    @Test
    public void testNoopMixinMetric() {
        MixinMetric mixinMetric = r.mixinMetric(r.createId("name"));
        Assert.assertEquals(NoopMixinMetric.INSTANCE, mixinMetric);
        Assert.assertEquals(NoopCounter.INSTANCE, mixinMetric.counter("c"));
        Assert.assertEquals(NoopTimer.INSTANCE, mixinMetric.timer("t"));
        Assert.assertEquals(NoopDistributionSummary.INSTANCE, mixinMetric.distributionSummary("t"));
        Assert.assertEquals(null, mixinMetric.gauge("t", new Gauge<Number>() {
            @Override
            public Number value() {
                return 1;
            }
        }));
    }

    @Test
    public void testNoopInfo() {
        Info info = r.info(r.createId("name"), new Info<String>() {
            @Override
            public String value() {
                return "xx";
            }
        });
        Assert.assertNull(null, info);
    }

    @Test
    public void testNoopGauge() {
        Gauge gauge = r.gauge(r.createId("name"), new Gauge() {
            @Override
            public Long value() {
                return 1l;
            }
        });
        Assert.assertNull(null, gauge);
    }

    @Test
    public void testNoopDistributionSummary() {
        DistributionSummary ds = r.distributionSummary(r.createId("name"));
        Assert.assertEquals(NoopDistributionSummary.INSTANCE, ds);
        ds.record(20);
        Assert.assertEquals(0, ds.count());
        Assert.assertEquals(0, ds.totalAmount());

    }

    @Test
    public void testNoopTimer() throws Exception {
        Timer timer = r.timer(r.createId("name"));
        Assert.assertEquals(NoopTimer.INSTANCE, timer);
        timer.record(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, timer.count());
        Assert.assertEquals(0, timer.totalTime());

        final AtomicInteger atomicInteger = new AtomicInteger(0);

        timer.record(new Runnable() {
            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        });
        timer.record(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                atomicInteger.incrementAndGet();
                return 0l;
            }
        });
        Assert.assertEquals(2, atomicInteger.get());
    }

}
