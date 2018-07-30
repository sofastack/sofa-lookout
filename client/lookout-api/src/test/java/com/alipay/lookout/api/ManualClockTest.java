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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/30
 */
public class ManualClockTest {
    @Test
    public void test() {
        ManualClock clock = new ManualClock();
        clock.setWallTime(100L);
        assertEquals(100L, clock.wallTime());
        assertEquals(100000L, clock.monotonicTime());

        clock.setWallTime(200L);
        assertEquals(200L, clock.wallTime());
        assertEquals(200000L, clock.monotonicTime());

        clock.setMonotonicTime(345678L);
        assertEquals(345L, clock.wallTime());
        assertEquals(345678L, clock.monotonicTime());
    }
}