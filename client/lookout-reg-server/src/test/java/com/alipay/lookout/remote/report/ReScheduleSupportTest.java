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
package com.alipay.lookout.remote.report;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.ManualClock;
import com.alipay.lookout.api.NoopRegistry;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.jdk8.Function;
import com.alipay.lookout.spi.MetricFilter;
import com.alipay.lookout.step.MeasurableScheduler;
import com.alipay.lookout.step.ScheduledService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alipay.sofa.common.log.Constants.SOFA_MIDDLEWARE_LOG_DISABLE_PROP_KEY;

/**
 * Created by kevin.luy@alipay.com on 2017/2/23.
 */
public class ReScheduleSupportTest {
    int                            phase         = 0;
    AtomicInteger                  phase1Counter = new AtomicInteger(0);
    AtomicInteger                  phase2Counter = new AtomicInteger(0);

    Function<MetricFilter, Object> function      = new Function<MetricFilter, Object>() {
                                                     @Override
                                                     public Object apply(MetricFilter o) {
                                                         if (phase == 0) {
                                                             System.out.println("11111");
                                                             phase1Counter.incrementAndGet();
                                                             return null;//phase1
                                                         }
                                                         //phase2
                                                         System.out.println("222");
                                                         phase2Counter.incrementAndGet();
                                                         return null;
                                                     }
                                                 };

    @BeforeClass
    public static void initClass() {
        System.setProperty(SOFA_MIDDLEWARE_LOG_DISABLE_PROP_KEY, "true");
    }

    @Test
    public void testReschedule() throws NoSuchFieldException, IllegalAccessException {
        ScheduledService scheduler = new MeasurableScheduler(NoopRegistry.INSTANCE, "poller", 2);

        LookoutConfig lookoutConfig = new LookoutConfig();
        lookoutConfig.setStepInterval(PRIORITY.HIGH, 1000);
        lookoutConfig.setStepInterval(PRIORITY.NORMAL, 1000);
        lookoutConfig.setStepInterval(PRIORITY.LOW, 1000);

        ReScheduleSupport support = new ReScheduleSupport(scheduler, lookoutConfig, Clock.SYSTEM);

        support.reschedulePoll(function);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Field field = ReScheduleSupport.class.getDeclaredField("taskResults");
        field.setAccessible(true);
        Set<ReScheduleSupport.TaskResult> set = (Set<ReScheduleSupport.TaskResult>) field
            .get(support);

        phase++;
        support.reschedulePoll(function); //restart

        //assert
        for (ReScheduleSupport.TaskResult result : set) {
            Assert.assertTrue(result.getFuture().isCancelled());
            Assert.assertFalse(result.getEnable().get());
        }
        Assert.assertTrue(phase1Counter.get() > 0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(phase2Counter.get() > 0);
    }

    @Test
    public void testInitDelay() {
        int stepSize = 60000;//60s ,6
        long now = 1522675416000l;//min+16s=416
        long min = now / stepSize * stepSize;
        //380-544 ,
        //System.out.println(stepBoundary + "," + (stepBoundary + stepSize));

        long val = ReScheduleSupport.getInitialDelay(60000, new ManualClock(now, 1l));
        Assert.assertEquals(now - min, val);
        System.out.println("=============================too small===================");
        now = 1522675385000l;
        min = now / stepSize * stepSize;
        System.out.println(min + "," + (min + stepSize));

        val = ReScheduleSupport.getInitialDelay(60000, new ManualClock(now, 1l));
        Assert.assertEquals(now - min + stepSize / 10, val);

        System.out.println("=============================too big===================");
        now = 1522675440000l - 3000l;
        min = now / stepSize * stepSize;
        long max = min + stepSize;
        System.out.println(min + "," + max);

        val = ReScheduleSupport.getInitialDelay(60000, new ManualClock(now, 1l));
        Assert.assertEquals(stepSize - (max - now + stepSize / 10), val);
    }
}
