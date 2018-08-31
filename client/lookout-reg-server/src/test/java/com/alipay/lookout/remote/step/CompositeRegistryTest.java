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

import com.alipay.lookout.api.ResettableStep;
import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.ManualClock;
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.common.LookoutConstants;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.report.SchedulerPoller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kevin.luy@alipay.com on 2017/2/15.
 */
public class CompositeRegistryTest {

    private CompositeRegistry      compositeRegistry;
    private ResettableStepRegistry resettableStepRegistry;

    @Before
    public void before() {
        compositeRegistry = new CompositeRegistry(Clock.SYSTEM);
        compositeRegistry.add(new DefaultRegistry());
        resettableStepRegistry = new ResettableStepRegistry(Clock.SYSTEM, new LookoutConfig());
        compositeRegistry.add(resettableStepRegistry);
    }

    @Test
    public void test_counter() {
        Id counterId = compositeRegistry.createId("counter");
        Counter counter = compositeRegistry.counter(counterId);
        assertThat(counter).isInstanceOf(ResettableStep.class);
        ResettableStep css = (ResettableStep) counter;
        css.setStep(10);
        LookoutCounter counter2 = (LookoutCounter) resettableStepRegistry.counter(counterId);
        assertThat(counter2.getStep()).isEqualTo(10);

        css.setStep(20);
        assertThat(counter2.getStep()).isEqualTo(20);
    }

    @Test
    public void testCompositeRemoteAndDefaultRegistry() {
        ManualClock clock = new ManualClock();
        CompositeRegistry registry = new CompositeRegistry(clock);
        MetricRegistry defaultRegistry = new DefaultRegistry(clock);
        registry.add(defaultRegistry);
        //config
        LookoutConfig lookoutConfig = new LookoutConfig();
        lookoutConfig.setStepInterval(PRIORITY.HIGH, 1000);
        lookoutConfig.setStepInterval(PRIORITY.NORMAL, 1000);
        lookoutConfig.setStepInterval(PRIORITY.LOW, 1000);

        StepRegistry remoteRegistry = new StepRegistry(clock, lookoutConfig);
        registry.add(remoteRegistry);
        SchedulerPoller schedulerPoller = new SchedulerPoller(remoteRegistry, registry,
            lookoutConfig, new LookoutRegistryTest.LogObserver());
        schedulerPoller.start();

        Id id = registry.createId("lookout.scheduler.poller").withTag(
            LookoutConstants.TAG_PRIORITY_KEY, PRIORITY.LOW.name());

        //composite
        MixinMetric mixin = registry.get(id);
        Counter c = mixin.counter("activeThreads");
        c.inc(7);

        //default and remote
        MixinMetric mixinMetric = defaultRegistry.get(id);
        Counter counter = mixinMetric.counter("activeThreads");
        MixinMetric mixinMetric2 = remoteRegistry.get(id);

        clock.setWallTime(lookoutConfig.stepMillis(PRIORITY.LOW));//时间步长来统计
        Counter counter2 = mixinMetric2.counter("activeThreads");
        System.out.println(counter.count());
        System.out.println(counter2.count());

        Assert.assertEquals(counter.count(), counter2.count());

    }

}
