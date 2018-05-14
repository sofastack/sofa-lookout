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

import com.alipay.lookout.api.Clock;

import java.util.concurrent.atomic.AtomicLong;

public class MockClock implements Clock {

    private final AtomicLong wall;
    private final AtomicLong monotonic;

    public MockClock() {
        this(0L, 0L);
    }

    public MockClock(long wallInit, long monotonicInit) {
        wall = new AtomicLong(wallInit);
        monotonic = new AtomicLong(monotonicInit);
    }

    @Override
    public long wallTime() {
        return wall.get();
    }

    @Override
    public long monotonicTime() {
        return monotonic.get();
    }

    public void setWallTime(long t) {
        wall.set(t);
    }

    public void setMonotonicTime(long t) {
        monotonic.set(t);
    }
}