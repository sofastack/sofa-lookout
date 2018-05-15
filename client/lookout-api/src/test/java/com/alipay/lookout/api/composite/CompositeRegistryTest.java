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

import com.alipay.lookout.api.*;
import com.alipay.lookout.api.info.Info;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by kevin.luy@alipay.com on 2018/5/16.
 */
public class CompositeRegistryTest {
    private static boolean registerExtentedMetrics = false;

    @Test
    public void testAddAndRemoveReg() {
        CompositeRegistry cr = new CompositeRegistry(Clock.SYSTEM);
        MockRegistry mockRegistry1 = new MockRegistry();
        MockRegistry mockRegistry2 = new MockRegistry();

        cr.add(mockRegistry1);
        cr.add(mockRegistry2);
        Assert.assertEquals(2, cr.getRegistries().size());

        cr.remove(mockRegistry1);
        Assert.assertEquals(1, cr.getRegistries().size());

        cr.clear();
        Assert.assertEquals(0, cr.getRegistries().size());

    }

    @Test
    public void testRegisterExtendMetrics() {
        CompositeRegistry cr = new CompositeRegistry(Clock.SYSTEM);
        cr.add(new MockRegistry());
        Assert.assertFalse(registerExtentedMetrics);
        cr.registerExtendedMetrics();
        Assert.assertTrue(registerExtentedMetrics);
    }

    public static class MockRegistry extends MetricRegistry {

        public MockRegistry() {
            super(Clock.SYSTEM);
        }

        @Override
        public void registerExtendedMetrics() {
            registerExtentedMetrics = true;
        }

        @Override
        public void register(Metric metric) {

        }

        @Override
        public <T extends Number> Gauge<T> gauge(Id id, Gauge<T> gauge) {
            return null;
        }

        @Override
        public void removeMetric(Id id) {

        }

        @Override
        public <I, Y extends Info<I>> Info info(Id id, Y info) {
            return null;
        }

        @Override
        public Counter counter(Id id) {
            return null;
        }

        @Override
        public DistributionSummary distributionSummary(Id id) {
            return null;
        }

        @Override
        public Timer timer(Id id) {
            return null;
        }

        @Override
        public MixinMetric mixinMetric(Id id) {
            return null;
        }

        @Override
        public <X extends Metric> X get(Id id) {
            return null;
        }

        @Override
        public Iterator<Metric> iterator() {
            return null;
        }
    }
}
