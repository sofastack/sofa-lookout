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

import com.alipay.lookout.api.DistributionSummary;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.Timer;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
class CompositeDistributionSummary extends CompositeMetric implements DistributionSummary {

    public CompositeDistributionSummary(Id id, Collection<Registry> registries) {
        super(id, registries);
    }

    @Override
    public void record(long amount) {
        for (Registry r : registries) {
            getMetric(r).record(amount);
        }
    }

    @Override
    public long count() {
        Iterator<Registry> it = registries.iterator();
        return it.hasNext() ? getMetric(it.next()).count() : 0L;
    }

    @Override
    public long totalAmount() {
        Iterator<Registry> it = registries.iterator();
        return it.hasNext() ? getMetric(it.next()).totalAmount() : 0L;
    }

    @Override
    public void enableBuckets(long[] buckets) {
        for (Registry r : registries) {
            getMetric(r).enableBuckets(buckets);
        }
    }

    @Override
    protected DistributionSummary getMetric(Registry registry) {
        return registry.distributionSummary(id);
    }

}
