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
import com.alipay.lookout.common.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public final class CompositeRegistry extends MetricRegistry {

    private final CopyOnWriteArraySet<Registry> registries;

    public CompositeRegistry(Clock clock) {
        super(clock);
        this.registries = new CopyOnWriteArraySet<Registry>();
    }

    @Override
    public void registerExtendedMetrics() {
        for (Registry r : registries) {
            if (r instanceof MetricRegistry) {
                ((MetricRegistry) r).registerExtendedMetrics();
            }
        }
    }

    /**
     * Find the first registry in the composite that is an instance of {@code c}. If no match is
     * found then null will be returned.
     */
    <T extends Registry> T find(Class<T> c) {
        for (Registry r : registries) {
            if (c.isAssignableFrom(r.getClass())) {
                return (T) r;
            }
        }
        return null;
    }

    /**
     * get all registry
     *
     * @return registries, it is modifiable.
     */
    public Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(this.registries);
    }

    /**
     * Add a registry to the composite.
     *
     * @param registry metricRegistry
     */
    public void add(MetricRegistry registry) {
        Assert.checkArg(!(registry instanceof CompositeRegistry),
            String.format("registry: %s can not be  a CompositeRegistry!", registry));
        registries.add(registry);
    }

    /**
     * Remove a registry from the composite.
     *
     * @param registry metricRegistry
     */
    public void remove(Registry registry) {
        registries.remove(registry);
    }

    /**
     * Remove all registries from the composite.
     */
    public void clear() {
        registries.clear();
    }

    @Override
    public <T extends Number> Gauge<T> gauge(Id id, Gauge<T> gauge) {

        for (Registry r : registries) {
            r.gauge(id, gauge);
        }

        return null;
    }

    @Override
    public void removeMetric(Id id) {
        for (Registry r : registries) {
            r.removeMetric(id);
        }
    }

    @Override
    public <I, Y extends Info<I>> Info info(Id id, Y info) {
        for (Registry r : registries) {
            r.info(id, info);
        }
        return null;
    }

    @Override
    public void register(Metric metric) {
        Assert.checkArg(metric instanceof Gauge, "Only Gauge support now");
        for (Registry r : registries) {
            r.register(metric);
        }
    }

    @Override
    public Counter counter(Id id) {
        return new CompositeCounter(id, registries);
    }

    @Override
    public DistributionSummary distributionSummary(Id id) {
        return new CompositeDistributionSummary(id, registries);
    }

    @Override
    public Timer timer(Id id) {
        return new CompositeTimer(id, clock(), registries);
    }

    @Override
    public MixinMetric mixinMetric(Id id) {
        for (Registry registry : registries) {
            registry.mixinMetric(id);
        }
        return new CompositeMixinMetric(id, clock(), registries);
    }

    @Override
    public <X extends Metric> X get(Id id) {
        Iterator<Registry> iterator = registries.iterator();
        Metric metric = iterator.hasNext() ? iterator.next().get(id) : null;
        if (metric == null) {
            return null;
        }
        if (MixinMetric.class.isAssignableFrom(metric.getClass())) {
            return (X) new CompositeMixinMetric(id, clock(), registries);
        }
        if (Gauge.class.isAssignableFrom(metric.getClass())) {
            return (X) metric;
        }
        if (Counter.class.isAssignableFrom(metric.getClass())) {
            return (X) new CompositeCounter(id, registries);
        }
        if (Timer.class.isAssignableFrom(metric.getClass())) {
            return (X) new CompositeTimer(id, clock(), registries);
        }
        if (DistributionSummary.class.isAssignableFrom(metric.getClass())) {
            return (X) new CompositeDistributionSummary(id, registries);
        }
        return null;
    }

    @Override
    public Iterator<Metric> iterator() {
        throw new UnsupportedOperationException();
    }
}
