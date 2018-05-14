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
package com.alipay.lookout.common;

import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.MetricObserver;

/**
 * Created by kevin.luy@alipay.com on 2017/10/4.
 */
public final class MetricObserverUtil {

    private MetricObserverUtil() {
    }

    public static boolean addMetricObservers(Registry registry, MetricObserver... metricObservers) {
        boolean flag = false;
        if (registry instanceof LookoutRegistry) {
            for (MetricObserver metricObserver : metricObservers) {
                ((LookoutRegistry) registry).addMetricObserver(metricObserver);
            }
            flag = true;
        }
        if (registry instanceof CompositeRegistry) {
            for (Registry reg : ((CompositeRegistry) registry).getRegistries()) {
                if (reg instanceof LookoutRegistry) {
                    for (MetricObserver metricObserver : metricObservers) {
                        ((LookoutRegistry) reg).addMetricObserver(metricObserver);
                    }
                    flag = true;
                }
            }
        }
        return flag;
    }

}
