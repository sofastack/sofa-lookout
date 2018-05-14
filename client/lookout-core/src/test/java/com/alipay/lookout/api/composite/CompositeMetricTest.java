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
package com.alipay.lookout.api.composite;

import com.alipay.lookout.api.*;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public class CompositeMetricTest {
    CompositeRegistry compositeRegistry;

    @Before
    public void init() {
        MetricRegistry r1 = new DefaultRegistry();
        MetricRegistry r2 = new DefaultRegistry();

        compositeRegistry = new CompositeRegistry(Clock.SYSTEM);
        compositeRegistry.add(r1);
        compositeRegistry.add(r2);
    }

    @Test
    public void testCompositeMixinMetric() {

        Id id = compositeRegistry.createId("haha");
        MixinMetric mixinMetric = compositeRegistry.mixinMetric(id);
        mixinMetric.gauge("gauge", new Gauge() {
            @Override
            public Integer value() {
                return 33;
            }
        });
        Assert.assertTrue(mixinMetric instanceof CompositeMixinMetric);
        MixinMetric mixinMetric2 = compositeRegistry.get(id);
        Assert.assertTrue(mixinMetric2 instanceof CompositeMixinMetric);

        //counter
        Counter counter = mixinMetric2.counter("count");
        counter.inc();
        Counter counter1 = mixinMetric.counter("count");
        Assert.assertEquals(1, counter.count());

        //timer
        Timer timer = mixinMetric.timer("time");
        timer.record(2, TimeUnit.SECONDS);
        Timer timer2 = mixinMetric2.timer("time");
        System.out.println(timer.totalTime());
        Assert.assertEquals(2000, timer.totalTime());

        //ds
        DistributionSummary ds = mixinMetric.distributionSummary("ds");
        ds.record(2);
        DistributionSummary ds2 = mixinMetric2.distributionSummary("ds");
        System.out.println(ds2.count());
        Assert.assertEquals(2, ds.totalAmount());

        //        //gauge
        //        Gauge g1=mixinMetric.component("gauge");
        //        Gauge g2=mixinMetric2.component("gauge");
        //        Assert.assertSame(g1,g2);
        //        Assert.assertEquals(g2.value(),g1.value());
        //        Assert.assertEquals(33,g1.value());

    }

    @Test
    public void testCompositeCounter() {
        Id id = compositeRegistry.createId("haha");

        Counter counter = compositeRegistry.counter(id);
        Assert.assertTrue(counter instanceof CompositeCounter);

        counter.inc();//必须这里显式执行下，下面才能拿到，否则上面还没实际去「子registry里拿到」

        Counter counter1 = compositeRegistry.get(id);
        Assert.assertTrue(counter1 instanceof CompositeCounter);

        counter.inc(2);
        Assert.assertEquals(3, counter1.count());

    }

    @Test
    public void testCompositeDistributionSummary() {
        Id id = compositeRegistry.createId("haha");

        DistributionSummary ds = compositeRegistry.distributionSummary(id);
        Assert.assertTrue(ds instanceof CompositeDistributionSummary);

        ds.record(1);//必须这里显式执行下，下面才能拿到，否则上面还没实际去「子registry里拿到」

        DistributionSummary ds2 = compositeRegistry.get(id);
        Assert.assertTrue(ds2 instanceof CompositeDistributionSummary);

        ds2.record(2);
        Assert.assertEquals(3, ds.totalAmount());
        Assert.assertEquals(2, ds.count());
    }

}
