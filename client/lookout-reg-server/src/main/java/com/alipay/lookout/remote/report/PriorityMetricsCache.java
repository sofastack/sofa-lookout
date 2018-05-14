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
package com.alipay.lookout.remote.report;

import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.common.utils.PriorityTagUtil;
import com.alipay.lookout.core.InfoWrapper;
import com.alipay.lookout.event.MetricRegistryListener;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by kevin.luy@alipay.com on 2017/3/15.
 */
public class PriorityMetricsCache implements MetricRegistryListener {

    private final Set<Metric> highMetircs   = Sets.newConcurrentHashSet();
    private final Set<Metric> normalMetircs = Sets.newConcurrentHashSet();
    private final Set<Metric> lowMetircs    = Sets.newConcurrentHashSet();

    public Set<Metric> getHighMetircs() {
        return highMetircs;
    }

    public Set<Metric> getNormalMetircs() {
        return normalMetircs;
    }

    public Set<Metric> getLowMetircs() {
        return lowMetircs;
    }

    @Override
    public void onRemoved(Metric metric) {
        if (metric instanceof InfoWrapper) {
            lowMetircs.remove(metric);
            return;
        }
        PRIORITY p = PriorityTagUtil.resolve(metric.id().tags());
        switch (p) {
            case HIGH:
                highMetircs.remove(metric);
                break;
            case NORMAL:
                normalMetircs.remove(metric);
                break;
            case LOW:
                lowMetircs.remove(metric);
                break;
        }
    }

    @Override
    public void onAdded(Metric metric) {
        if (metric instanceof InfoWrapper) {
            lowMetircs.add(metric);
            return;
        }
        PRIORITY p = PriorityTagUtil.resolve(metric.id().tags());
        switch (p) {
            case HIGH:
                highMetircs.add(metric);
                break;
            case NORMAL:
                normalMetircs.add(metric);
                break;
            case LOW:
                lowMetircs.add(metric);
                break;
        }
    }
}
