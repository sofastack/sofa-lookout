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
package com.alipay.lookout.step;

import com.alipay.lookout.api.ManualClock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StepLongTest {

    private final ManualClock clock = new ManualClock();

    @Before
    public void init() {
        clock.setWallTime(0L);
    }

    @Test
    public void empty() {
        StepLong v = new StepLong(0L, clock, 10L);
        Assert.assertEquals(0L, v.getCurrent().get());
        Assert.assertEquals(0L, v.poll());
    }

    @Test
    public void increment() {
        StepLong v = new StepLong(0L, clock, 10L);
        v.getCurrent().incrementAndGet();
        Assert.assertEquals(1L, v.getCurrent().get());
        Assert.assertEquals(0L, v.poll());
    }

    @Test
    public void incrementAndCrossStepBoundary() {
        StepLong v = new StepLong(0L, clock, 10L);
        //step1-窗口内，中加1
        v.getCurrent().incrementAndGet();
        clock.setWallTime(10L);
        Assert.assertEquals(0L, v.getCurrent().get());
        Assert.assertEquals(1L, v.poll());//previous

        //step2-窗口内:
        clock.setWallTime(21L);
        Assert.assertEquals(1L, v.previous());
        //step3-窗口内;由于在step2窗口没有滚动触发，那么到了step3的窗口，则previous就是0了。
        clock.setWallTime(32L);
        v.pollAsRate();
        System.out.println(v.previous());
        Assert.assertEquals(0L, v.previous());
    }

    @Test
    public void missedRead() {
        StepLong v = new StepLong(0L, clock, 10L);
        v.getCurrent().incrementAndGet();
        clock.setWallTime(20L);
        Assert.assertEquals(0L, v.getCurrent().get());
        Assert.assertEquals(0L, v.poll());
    }

    @Test
    public void test_setStep() {
        StepLong v = new StepLong(0, clock, 10L);
        v.getCurrent().incrementAndGet();
        clock.setWallTime(10L);
        Assert.assertEquals(0L, v.getCurrent().get());
        Assert.assertEquals(1L, v.poll());

        v.getCurrent().incrementAndGet();
        v.getCurrent().incrementAndGet();
        clock.setWallTime(20L);
        Assert.assertEquals(0L, v.getCurrent().get());
        Assert.assertEquals(2L, v.poll());

        // 将步长修改成20L, 会导致 StepLong 立即清空内部数据
        v.setStep(20L);

        v.getCurrent().incrementAndGet();
        v.getCurrent().incrementAndGet();
        v.getCurrent().incrementAndGet();
        v.getCurrent().incrementAndGet();
        clock.setWallTime(50L);
        Assert.assertEquals(0L, v.getCurrent().get());
        Assert.assertEquals(4L, v.poll());

    }
}
