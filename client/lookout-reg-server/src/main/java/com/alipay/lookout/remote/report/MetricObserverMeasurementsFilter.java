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

import com.alipay.lookout.report.MetricObserver;

import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2017/8/30.
 */
public interface MetricObserverMeasurementsFilter<T> {

    /**
     * 过滤出能处理的 measurements
     *
     * @param measurements 待筛选 measurements
     * @param metricObserver metric observer
     * @return 过滤后的结果
     */
    List<T> filter(List<T> measurements, MetricObserver metricObserver);
}
