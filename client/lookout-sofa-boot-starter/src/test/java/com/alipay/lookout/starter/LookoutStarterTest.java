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
package com.alipay.lookout.starter;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.dropwizard.metrics.DropWizardMetricsRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2018/5/15.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LookoutStarterTest.class)
@Component
@ComponentScan
@SpringBootApplication
public class LookoutStarterTest {

    @Autowired
    Registry registry;

    @Test
    public void testDropwizardMetrics() {
        Id id = registry.createId("test_status").withTag("k1", "v1");
        MixinMetric mixinMetric = registry.mixinMetric(id);
        mixinMetric.distributionSummary("dstest").record(100);
        mixinMetric.counter("counttest").inc();
        mixinMetric.timer("timetest").record(2, TimeUnit.SECONDS);
        mixinMetric.gauge("guagetest", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 1;
            }
        });

        Assert.assertTrue(registry instanceof CompositeRegistry);
        CompositeRegistry compositeRegistry = (CompositeRegistry) registry;
        DropWizardMetricsRegistry dwr = null;
        for (Registry r : compositeRegistry.getRegistries()) {
            if (r instanceof DropWizardMetricsRegistry) {
                dwr = (DropWizardMetricsRegistry) r;
                break;
            }
        }

        MixinMetric mixinMetric2 = dwr.mixinMetric(id);
        Assert.assertEquals(1, mixinMetric2.counter("counttest").count());
        Assert.assertEquals(1, mixinMetric2.timer("timetest").count());
        Assert.assertEquals(1, mixinMetric2.distributionSummary("dstest").count());

    }

}
