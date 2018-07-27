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

import com.alipay.lookout.remote.report.MetricObserverMeasurementsFilter;
import com.alipay.lookout.report.MetricObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by kevin.luy@alipay.com on 2017/8/17.
 */
public final class MetricObserverComposite<T> implements MetricObserver<T> {
    private final List<MetricObserver<T>>                   metricObserverList                = new CopyOnWriteArrayList<MetricObserver<T>>();
    private final List<MetricObserverMeasurementsFilter<T>> metricObserverMeasurementsFilters = new ArrayList<MetricObserverMeasurementsFilter<T>>();

    private volatile boolean                                enabled                           = true;

    public MetricObserverComposite(MetricObserver<T>... metricObservers) {
        if (metricObservers != null) {
            metricObserverList.addAll(Arrays.asList(metricObservers));
        }
    }

    public void addMetricObserver(MetricObserver<T> metricObserver) {
        metricObserverList.add(metricObserver);
    }

    public List<MetricObserver<T>> getMetricObservers() {
        return Collections.unmodifiableList(metricObserverList);
    }

    @Override
    public void update(List<T> measures, Map<String, String> metadata) {
        for (MetricObserver observer : metricObserverList) {
            if (observer.isEnable()) {
                observer.update(filterInterestedMeasurements(measures, observer), metadata);
            }
        }
    }

    public List<T> filterInterestedMeasurements(List<T> allMeasurements,
                                                MetricObserver metricObserver) {
        if (metricObserverMeasurementsFilters.isEmpty()) {
            return allMeasurements;
        }
        List<T> tmp = allMeasurements;
        //filter measurements
        for (MetricObserverMeasurementsFilter measurementsFilter : metricObserverMeasurementsFilters) {
            tmp = measurementsFilter.filter(tmp, metricObserver);
        }
        if (tmp == null) {
            return new ArrayList<T>(0);
        }
        return tmp;
    }

    @Override
    public boolean isEnable() {
        if (!enabled) {
            return false;
        }
        for (MetricObserver mo : metricObserverList) {
            if (mo.isEnable()) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return metricObserverList.size();
    }

    public void addMetricObserverMeasurementsFilter(MetricObserverMeasurementsFilter metricObserverMeasurementsFilter) {
        metricObserverMeasurementsFilters.add(metricObserverMeasurementsFilter);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
