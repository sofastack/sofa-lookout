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

import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.api.info.Info;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by kevin.luy@alipay.com on 2017/2/20.
 */
public class LookoutTest {
    @Before
    public void setUp() throws Exception {
        Field f = Lookout.class.getDeclaredField("atomicRegistryReference");
        f.setAccessible(true);
        AtomicReference atomicReference = (AtomicReference) f.get(null);
        atomicReference.set(NoopRegistry.INSTANCE);

    }

    @Test
    public void testLookout() {
        Registry registry = Lookout.registry();
        Assert.assertSame(NoopRegistry.INSTANCE, registry);
        Counter counter = registry.counter(registry.createId("aa"));
        counter.inc();
        counter.dec();
        counter.inc(2);
        counter.dec(2);
    }

    @Test
    public void testLookoutReset() {
        Lookout.registry();
        Lookout.setRegistry(new MockRegistry());
        Assert.assertSame(MockRegistry.class, Lookout.registry().getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void testLookoutMoreThanOnce() {
        Lookout.registry();
        Lookout.setRegistry(new MockRegistry());
        Lookout.setRegistry(new MockRegistry());
    }

    @Test
    public void testLookoutSetNoopRegistry() {
        Lookout.registry();
        Lookout.setRegistry(NoopRegistry.INSTANCE);
        Lookout.setRegistry(NoopRegistry.INSTANCE);
    }

    static class MockRegistry implements Registry {

        @Override
        public Clock clock() {
            return Clock.SYSTEM;
        }

        @Override
        public Id createId(String name) {
            return NoopId.INSTANCE;
        }

        @Override
        public Id createId(String name, Iterable<Tag> tags) {
            return NoopId.INSTANCE;
        }

        @Override
        public Id createId(String name, Map<String, String> tags) {
            return NoopId.INSTANCE;
        }

        @Override
        public void register(Metric metric) {

        }

        @Override
        public <T extends Number> Gauge<T> gauge(Id id, Gauge<T> gauge) {
            return null;
        }

        @Override
        public <I, Y extends Info<I>> Info info(Id id, Y info) {
            return null;
        }

        @Override
        public void removeMetric(Id id) {

        }

        @Override
        public Counter counter(Id id) {
            return NoopCounter.INSTANCE;
        }

        @Override
        public DistributionSummary distributionSummary(Id id) {
            return NoopDistributionSummary.INSTANCE;
        }

        @Override
        public Timer timer(Id id) {
            return NoopTimer.INSTANCE;
        }

        @Override
        public MixinMetric mixinMetric(Id id) {
            return NoopMixinMetric.INSTANCE;
        }

        @Override
        public <X extends Metric> X get(Id id) {
            return null;
        }

        @Override
        public Iterator<Metric> iterator() {
            return null;
        }

        @Override
        public void propagate(String msg, Throwable t) {

        }
    }
}
