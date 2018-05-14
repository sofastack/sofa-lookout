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
import com.alipay.lookout.api.info.AutoPollFriendlyInfo;
import com.alipay.lookout.api.info.AutoPollSuggestion;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kevin.luy@alipay.com on 2017/2/15.
 */
public class RegistryTest {
    Clock clock = Clock.SYSTEM;

    @Test
    public void testNoopRegistry() throws Exception {
        final Registry registry = NoopRegistry.INSTANCE;
        //counter
        Counter counter = registry.counter(registry.createId("counter"));
        counter.inc();
        counter.dec();
        counter.count();
        //timer
        Timer timer = registry.timer(registry.createId("timer"));
        timer.record(2, TimeUnit.SECONDS);
        final AtomicInteger x = new AtomicInteger(0);

        timer.record(new Runnable() {
            @Override
            public void run() {
                x.incrementAndGet();
            }
        });
        timer.record(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                x.incrementAndGet();
                return null;
            }
        });
        Assert.assertEquals(2, x.intValue());

        //ds
        DistributionSummary ds = registry.distributionSummary(registry.createId("ds"));
        ds.record(1);
        ds.count();
        ds.totalAmount();

        //gauge
        registry.gauge(registry.createId("gauge"), new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 3;
            }
        });
        registry.removeMetric(registry.createId("gauge"));

        //info

        registry.info(registry.createId("jvm.system.properties"), new Info<Map>() {
            @Override
            public Map value() {
                return System.getProperties();
            }
        });

        registry.info(registry.createId("sofa.config"),
            new AutoPollFriendlyInfo<Map<String, String>>() {

                @Override
                public AutoPollSuggestion autoPollSuggest() {
                    // AutoPollSuggestion.NEVEL_AUTO_POLL;
                    //AutoPollSuggestion.POLL_WHEN_UPDATED;
                    //new AutoPollSuggestion(24, TimeUnit.DAYS);
                    return AutoPollSuggestion.POLL_WHEN_UPDATED;
                }

                @Override
                public long lastModifiedTime() {
                    return -1L;
                }

                @Override
                public Map<String, String> value() {
                    return new HashMap();
                }
            });

        //minxin

        MixinMetric mixinMetric = registry.mixinMetric(registry.createId("mixinMetric"));
        Counter compCounter = mixinMetric.counter("xx");
        Timer compTimer = mixinMetric.timer("time");
        DistributionSummary compDs = mixinMetric.distributionSummary("ds");
        mixinMetric.gauge("compGauge", new Gauge<Long>() {
            @Override
            public Long value() {
                return 1l;
            }
        });
    }

    @Test
    public void testDefaultRegistry() throws Exception {
        final Registry registry = new DefaultRegistry(clock);

        //counter
        Counter counter = registry.counter(registry.createId("counter"));
        Assert.assertNotNull(counter);
        counter.inc();
        Assert.assertEquals(1, counter.count());
        counter.inc(6);
        Assert.assertEquals(7, counter.count());
        counter.dec(2);
        Assert.assertEquals(5, counter.count());
        counter.dec();
        Assert.assertEquals(4, counter.count());

        //timer
        Timer timer = registry.timer(registry.createId("timer"));
        Assert.assertNotNull(timer);
        timer.record(2, TimeUnit.SECONDS);
        Assert.assertEquals(1, timer.count());
        Assert.assertEquals(2000, timer.totalTime());
        timer.record(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(5);
                return 1;
            }
        });
        timer.record(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Assert.assertEquals(3, timer.count());
        System.out.println(timer.totalTime());
        Assert.assertTrue(timer.totalTime() >= 2000 + 10);

        //ds

        DistributionSummary ds = registry.distributionSummary(registry.createId("ds"));
        DistributionSummary ds2 = registry.get(registry.createId("ds"));
        Assert.assertSame(ds, ds2);
        Assert.assertNotNull(ds);
        ds.record(2);
        ds.record(4);

        Assert.assertEquals(2, ds.count());
        Assert.assertEquals(6, ds.totalAmount());

        //gauge
        registry.gauge(registry.createId("gauge"), new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 1;
            }
        });
        Gauge gauge = (Gauge) registry.get(registry.createId("gauge"));
        Assert.assertEquals(1, gauge.value());

    }

}
