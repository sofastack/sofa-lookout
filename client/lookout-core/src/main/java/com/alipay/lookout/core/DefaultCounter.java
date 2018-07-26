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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counter implementation for the default registry.
 */
final class DefaultCounter implements Counter {

    private final Clock      clock;
    private final Id         id;
    private final AtomicLong count;

    DefaultCounter(Clock clock, Id id) {
        this.clock = clock;
        this.id = id;
        this.count = new AtomicLong(0L);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Indicator<Long> measure() {
        long now = clock.wallTime();
        long v = count.get();
        return new Indicator<Long>(now, id, v);
    }

    @Override
    public void inc() {
        count.incrementAndGet();
    }

    @Override
    public void inc(long amount) {
        count.addAndGet(amount);
    }

    @Override
    public void dec() {
        dec(1);
    }

    @Override
    public void dec(long n) {
        count.addAndGet(-n);
    }

    @Override
    public long count() {
        return count.get();
    }
}
