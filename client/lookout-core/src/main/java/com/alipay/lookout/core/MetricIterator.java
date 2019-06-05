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

import com.alipay.lookout.api.Metric;

import java.util.Iterator;

/**
 * @author zhangzhuo
 * @version $Id: MetricIterator.java, v 0.1 2018年09月17日 下午2:29 zhangzhuo Exp $
 */
public class MetricIterator implements Iterator<Metric> {

    private final Iterator<Metric> baseIterator;

    private Iterator<Metric>       extendIterator;

    public MetricIterator(Iterator<Metric> baseIterator) {
        this.baseIterator = baseIterator;
    }

    @Override
    public boolean hasNext() {
        if (extendIterator != null && extendIterator.hasNext()) {
            return true;
        }
        return baseIterator.hasNext();
    }

    @Override
    public Metric next() {
        if (extendIterator != null && extendIterator.hasNext()) {
            return extendIterator.next();
        }
        Metric metric = baseIterator.next();
        if (metric instanceof MetricIterable) {
            MetricIterable bucketCounter = (MetricIterable) metric;
            extendIterator = bucketCounter.iterator();
        }
        return metric;
    }

    @Override
    public void remove() {
        baseIterator.remove();
    }
}