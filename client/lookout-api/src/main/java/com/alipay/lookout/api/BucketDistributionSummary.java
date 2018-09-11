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

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangzhuo
 * @version $Id: BucketDistributionSummary.java, v 0.1 2018年09月07日 上午11:47 zhangzhuo Exp $
 */
public abstract class BucketDistributionSummary implements DistributionSummary {

    private long[]       buckets;

    private AtomicLong[] counts;

    @Override
    public void enableBuckets(long[] buckets) {
        this.buckets = buckets;
        this.counts = new AtomicLong[buckets.length + 1];
    }

    protected void recordBucket(long amount) {
        if (buckets == null) {
            return;
        }
        int i = 0;
        for (; i < buckets.length; ++i) {
            if (amount <= buckets[i]) {
                break;
            }
        }
        if (counts[i] == null) {
            counts[i] = new AtomicLong();
        }
        counts[i].incrementAndGet();
    }

    private String getBucketTag(int i) {
        if (i == 0) {
            return "0-" + buckets[0];
        }
        if (i == buckets.length) {
            return buckets[buckets.length - 1] + "-";
        }
        return buckets[i - 1] + "-" + buckets[i];
    }

    public Iterator<Metric> bucketMetricIterator() {
        return new Iterator<Metric>() {

            int            i = 0;

            private Metric metric;

            @Override
            public boolean hasNext() {
                if (metric == null) {
                    while (i < counts.length) {
                        if (counts[i] != null) {
                            metric = new BucketMetric(i);
                            i++;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                return metric != null;
            }

            @Override
            public Metric next() {
                Metric m = metric;
                metric = null;
                return m;
            }

            @Override
            public void remove() {

            }
        };
    }

    class BucketMetric implements Metric {

        Id  id;

        int i;

        public BucketMetric(int i) {
            this.i = i;
            String bucketTag = getBucketTag(i);
            id = BucketDistributionSummary.this.id().withTag(BUCKET_TAG_NAME, bucketTag);
        }

        @Override
        public Id id() {
            return id;
        }

        @Override
        public Indicator measure() {
            long now = Clock.SYSTEM.wallTime();
            Indicator indicator = new Indicator(now, id).addMeasurement(Statistic.buckets.name(),
                counts[i].getAndSet(0));
            return indicator;
        }

    }
}