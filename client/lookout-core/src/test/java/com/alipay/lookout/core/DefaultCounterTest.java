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
package com.alipay.lookout.core;

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.ManualClock;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DefaultCounterTest {

    private final ManualClock clock = new ManualClock();

    @Test
    public void testInit() {
        Counter c = new DefaultCounter(clock, NoopId.INSTANCE);
        Assert.assertEquals(c.count(), 0L);
    }

    @Test
    public void testinc() {
        Counter c = new DefaultCounter(clock, NoopId.INSTANCE);
        c.inc();
        Assert.assertEquals(c.count(), 1L);
        c.inc();
        c.inc();
        Assert.assertEquals(c.count(), 3L);
    }

    @Test
    public void testincAmount() {
        Counter c = new DefaultCounter(clock, NoopId.INSTANCE);
        c.inc(42);
        Assert.assertEquals(c.count(), 42L);
    }

}
