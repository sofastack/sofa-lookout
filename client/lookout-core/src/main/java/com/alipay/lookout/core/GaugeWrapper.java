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
package com.alipay.lookout.core;

import com.alipay.lookout.api.*;

/**
 * Created by kevin.luy@alipay.com on 2017/2/20.
 */
public final class GaugeWrapper<T extends Number> implements Gauge<T>, Metric {
    private Gauge<T> gauge;
    private Clock    clock;
    private Id       id;

    public GaugeWrapper(Id id, Gauge<T> gauge, Clock clock) {
        this.id = id;
        this.gauge = gauge;
        this.clock = clock;
    }

    public Gauge getOriginalOne() {
        return gauge;
    }

    @Override
    public Indicator measure() {
        return new Indicator(clock.wallTime(), id(), value().doubleValue());
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public T value() {
        return gauge.value();
    }
}
