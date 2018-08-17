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

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.ManualClock;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/2/24.
 */
public class LookoutMetricTest {

    @Test
    public void testLookoutTime() {
        ManualClock clock = new ManualClock();
        StepRegistry r = new StepRegistry(clock, new LookoutConfig());
        Id id = r.createId("aa");
        LookoutTimer timer = new LookoutTimer(id, clock, 1000);
        timer.record(100, TimeUnit.SECONDS);
        timer.record(200, TimeUnit.SECONDS);
        timer.record(300, TimeUnit.SECONDS);
        clock.setWallTime(1000);

        String str = "";
        for (Object m : timer.measure().measurements()) {
            System.out.println(m);
            str += m;
        }
        Assert.assertTrue(str.contains("totalTime:600.0"));
        Assert.assertTrue(str.contains("max:300.0"));
        Assert.assertTrue(str.contains("elapPerExec:200.0"));
    }

    @Test
    public void testLookoutCounter() {
        ManualClock clock = new ManualClock();
        //StepRegistry r = new StepRegistry(clock, new LookoutConfig());
        Id id = new DefaultRegistry().createId("aa");
        LookoutCounter counter = new LookoutCounter(id, clock, 500);
        counter.inc();
        counter.inc(7);
        counter.dec();
        counter.dec(2);

        clock.setWallTime(500);
        String str = "";
        for (Object m : counter.measure().measurements()) {
            System.out.println(m);
            str += m;
        }
        Assert.assertTrue(str.contains("count:5"));
        Assert.assertTrue(str.contains("rate:10.0"));
    }

    @Test
    public void testLookoutCounterPollerLater() {
        ManualClock clock = new ManualClock();
        StepRegistry r = new StepRegistry(clock, new LookoutConfig());
        Id id = r.createId("aa");
        LookoutCounter counter = new LookoutCounter(id, clock, 1000);
        counter.inc();
        clock.setWallTime(1000);//win 1
        String str = "";
        for (Object m : counter.measure().measurements()) {
            System.out.println(m);
            str += m;
        }
        clock.setWallTime(2000);//win 2

        counter.inc();
        clock.setWallTime(4000);//win 3-4
        str = "";
        for (Object m : counter.measure().measurements()) {
            System.out.println(m);
            str += m;
        }

        //        Assert.assertTrue(str.contains("count:5"));
        //        Assert.assertTrue(str.contains("rate:10.0"));
    }
}
