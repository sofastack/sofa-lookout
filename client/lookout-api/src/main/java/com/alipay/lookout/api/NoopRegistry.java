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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Registry implementation that does nothing.
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public final class NoopRegistry implements Registry {

    public static final NoopRegistry INSTANCE = new NoopRegistry();

    private NoopRegistry() {
    }

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
    public void propagate(String msg, Throwable t) {
        throw new RuntimeException(msg, t);
    }

    @Override
    public Id createId(String name, Map<String, String> tags) {
        return NoopId.INSTANCE;
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
    public Metric get(Id id) {
        return null;
    }

    @Override
    public Iterator<Metric> iterator() {
        return Collections.<Metric> emptyList().iterator();
    }

    @Override
    public void removeMetric(Id id) {

    }

    @Override
    public MixinMetric mixinMetric(Id id) {
        return NoopMixinMetric.INSTANCE;
    }

}
