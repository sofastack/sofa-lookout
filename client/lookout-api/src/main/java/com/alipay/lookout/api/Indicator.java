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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
public final class Indicator<T> {
    private final long                timestamp;

    private final Id                  id;

    //multi values;
    private final Set<Measurement<T>> measurements;

    public Indicator(long timestamp, Id id) {
        this.timestamp = timestamp;
        this.id = id;
        this.measurements = new HashSet<Measurement<T>>();
    }

    // only one measurement,quick construct;
    public Indicator(long timestamp, Id id, T value) {
        this.timestamp = timestamp;
        this.id = id;
        this.measurements = Collections.singleton(new Measurement<T>(value));
    }

    public Indicator<T> addMeasurement(String name, T value) {
        measurements.add(new Measurement<T>(name, value));
        return this;
    }

    public Indicator<T> addMeasurement(Measurement<T> measurement) {
        measurements.add(measurement);
        return this;
    }

    public Id id() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Collection<Measurement<T>> measurements() {
        return measurements;
    }

    @Override
    public String toString() {
        return "Indicator{" + "timestamp=" + timestamp + ", id=" + id + ", measurements="
               + measurements + '}';
    }
}
