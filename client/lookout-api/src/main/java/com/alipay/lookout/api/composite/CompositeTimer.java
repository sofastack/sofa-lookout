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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.Timer;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
class CompositeTimer extends CompositeMetric implements Timer {
    private final Clock clock;

    public CompositeTimer(Id id, Clock clock, Collection<Registry> registries) {
        super(id, registries);
        this.clock = clock;
    }

    @Override
    protected Timer getMetric(Registry registry) {
        return registry.timer(id);
    }

    @Override
    public void record(long amount, TimeUnit unit) {
        for (Registry r : registries) {
            getMetric(r).record(amount, unit);
        }
    }

    @Override
    public <T> T record(Callable<T> f) throws Exception {
        final long s = clock.monotonicTime();
        try {
            return f.call();
        } finally {
            final long e = clock.monotonicTime();
            record(e - s, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void record(Runnable f) {
        final long s = clock.monotonicTime();
        try {
            f.run();
        } finally {
            final long e = clock.monotonicTime();
            record(e - s, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public long count() {
        Iterator<Registry> it = registries.iterator();
        return it.hasNext() ? getMetric(it.next()).count() : 0L;
    }

    @Override
    public long totalTime() {
        Iterator<Registry> it = registries.iterator();
        return it.hasNext() ? getMetric(it.next()).totalTime() : 0L;
    }
}
