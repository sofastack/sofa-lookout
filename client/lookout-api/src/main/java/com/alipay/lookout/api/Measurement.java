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

import com.alipay.lookout.common.Assert;

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public final class Measurement<T> {
    public final static String EMPTY_STR = "value";
    private String             name;
    private final T            value;

    /**
     * Create a new instance.
     *
     * @param name  name
     * @param value value
     */
    public Measurement(String name, T value) {
        Assert.notNull(name, "name is null!");
        this.name = name;
        this.value = value;
    }

    public Measurement(T value) {
        // name 默认是 "value"
        this.name = EMPTY_STR;
        this.value = value;
    }

    public String name() {
        return name;
    }

    /**
     * Value for the measurement.
     *
     * @return value
     */
    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof Measurement))
            return false;
        Measurement other = (Measurement) obj;

        boolean valueEqual = (value == other.value());
        if (!valueEqual && value instanceof Double && other.value instanceof Double) {
            valueEqual = Double.compare((Double) value, (Double) other.value) == 0;
        }
        return name.equals(other.name) && valueEqual;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hc = prime;
        hc = prime * hc + name.hashCode();
        int valueHashCode = 0;
        if (value instanceof Double) {
            valueHashCode = Double.valueOf((Double) value).hashCode();
        } else {
            valueHashCode = value.hashCode();
        }
        hc = prime * hc + valueHashCode;
        return hc;
    }

    @Override
    public String toString() {
        return "Measurement(" + name + ":" + value + ")";
    }

}
