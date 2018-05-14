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

import com.alipay.lookout.api.*;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.core.config.LookoutConfig;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/10/5.
 */
public class DropWizardMetricsRegistryTest {

    @Test
    public void testDropwizardOverMaxCount() {
        MetricRegistry reg = new MetricRegistry();
        LookoutConfig lookoutConfig = new LookoutConfig();
        lookoutConfig.setProperty(LookoutConfig.LOOKOUT_MAX_METRICS_NUMBER, 1);
        DropWizardMetricsRegistry dwreg = new DropWizardMetricsRegistry(Clock.SYSTEM, reg,
            lookoutConfig);
        Counter counter1 = dwreg.counter(dwreg.createId("a.b"));
        Counter counter2 = dwreg.counter(dwreg.createId("a.b.c"));
        System.out.println(counter2.getClass().getCanonicalName());
        Assert.assertTrue(counter2.getClass().getCanonicalName().contains("NoopCounter"));
    }

    @Test
    public void testDropwizardCount() {
        MetricRegistry reg = new MetricRegistry();
        DropWizardMetricsRegistry dwreg = new DropWizardMetricsRegistry(Clock.SYSTEM, reg);
        Counter counter1 = dwreg.counter(dwreg.createId("a.b.c"));
        counter1.inc(5);

        com.codahale.metrics.Counter counter2 = reg.counter("a.b.c");
        System.out.println(counter2.getCount());
        Assert.assertEquals(5, counter2.getCount());
    }

    @Test
    public void testDropwizardGuage() {
        MetricRegistry reg = new MetricRegistry();
        DropWizardMetricsRegistry dwreg = new DropWizardMetricsRegistry(Clock.SYSTEM, reg);
        Gauge gauge = dwreg.gauge(dwreg.createId("a.b.c"), new Gauge<Number>() {
            @Override
            public Number value() {
                return 2;
            }
        });
        Iterator<com.codahale.metrics.Gauge> it = reg.getGauges().values().iterator();
        int num = (Integer) it.next().getValue();
        System.out.println(num);
        Assert.assertEquals(2, num);
    }

    @Test
    public void testDropwizardDs() {
        MetricRegistry reg = new MetricRegistry();
        DropWizardMetricsRegistry dwreg = new DropWizardMetricsRegistry(Clock.SYSTEM, reg);
        DistributionSummary ds = dwreg.distributionSummary(dwreg.createId("a.b.c"));
        ds.record(3);
        ds.record(3);
        Histogram histogram = reg.histogram("a.b.c");
        System.out.println(histogram.getCount());
        Assert.assertEquals(2, histogram.getCount());
    }

    @Test
    public void testDropwizardTime() {
        MetricRegistry reg = new MetricRegistry();
        DropWizardMetricsRegistry dwreg = new DropWizardMetricsRegistry(Clock.SYSTEM, reg);
        Timer timer = dwreg.timer(dwreg.createId("a.b.c"));
        timer.record(123, TimeUnit.SECONDS);
        com.codahale.metrics.Timer timer2 = reg.timer("a.b.c");
        System.out.println(timer2.getCount());
        Assert.assertEquals(1, timer2.getCount());

    }

    @Test
    public void testDropwizardMixin() {
        MetricRegistry reg = new MetricRegistry();
        DropWizardMetricsRegistry dwreg = new DropWizardMetricsRegistry(Clock.SYSTEM, reg);

        MixinMetric mixinMetric1 = dwreg.mixinMetric(dwreg.createId("x.y"));
        Counter counter = mixinMetric1.counter("count");
        counter.inc();

        long count = reg.counter("x.y.count").getCount();
        Assert.assertEquals(1, count);

        //gauge
        MixinMetric mixinMetric2 = dwreg.mixinMetric(dwreg.createId("x.y"));
        mixinMetric2.gauge("gauge", new Gauge<Long>() {
            @Override
            public Long value() {
                return 123l;
            }
        });
        com.codahale.metrics.Gauge gauge = reg.getGauges().values().iterator().next();
        Assert.assertEquals(123l, gauge.getValue());
    }

}
