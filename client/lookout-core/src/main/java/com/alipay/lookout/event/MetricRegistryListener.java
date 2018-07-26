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
package com.alipay.lookout.event;

import com.alipay.lookout.api.Metric;

/**
     * registry events listener
     * Created by kevin.luy@alipay.com on 2017/3/15.
     */
public interface MetricRegistryListener {

    /**
     * action on a event when a metric is removed from the registry
     *
    * @param metric a metric
    */
    void onRemoved(Metric metric);

    /**
     * action on a event when a metric is added from the registry
     *
     * @param metric a metric
     */
    void onAdded(Metric metric);

}
