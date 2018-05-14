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

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.Registry;

import java.util.Collection;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
abstract class CompositeMetric implements Metric {
    /**
     * Identifier for the meter.
     */
    protected final Id                   id;

    /**
     * Underlying registries that are keeping the data.
     */
    protected final Collection<Registry> registries;

    public CompositeMetric(Id id, Collection<Registry> registries) {
        this.id = id;
        this.registries = registries;
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Indicator measure() {
        throw new UnsupportedOperationException();
    }

    protected abstract <T extends Metric> T getMetric(Registry registry);
}
