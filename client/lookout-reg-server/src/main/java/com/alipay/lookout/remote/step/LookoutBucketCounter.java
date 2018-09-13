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

import com.alipay.lookout.api.*;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangzhuo
 * @version $Id: BucketDistributionSummary.java, v 0.1 2018年09月07日 上午11:47 zhangzhuo Exp $
 */
public abstract class LookoutBucketCounter implements Metric, Iterable<Metric> {

    private static final String   BUCKET_TAG_NAME = "_bucket";

    protected Clock               clock;

    private long                  step;

    private long[]                buckets;

    private volatile AtomicLong[] counts;

    private volatile AtomicLong[] prevCounts;

    private final AtomicLong      lastInitPos;

    public LookoutBucketCounter(Clock clock, long step) {
        this.clock = clock;
        this.step = step;
        lastInitPos = new AtomicLong(clock.wallTime() / step);
    }

    public void setStep(long step) {
        this.step = step;
    }

    public void buckets(long[] buckets) {
        this.buckets = buckets;
        this.counts = new AtomicLong[buckets.length + 1];
    }

    public void recordBucket(long amount) {
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

    private void roll() {
        final long stepTime = clock.wallTime() / step;
        final long lastInit = lastInitPos.get();
        // 如果正好到达下一个步长区间，则并发竞争成功的线程做实际更新；
        if (lastInit < stepTime && lastInitPos.compareAndSet(lastInit, stepTime)) {
            // 每次取出当前值，并设置初始值重新开始新一轮计数；
            if (lastInit == stepTime - 1) {
                prevCounts = counts;
            } else {
                prevCounts = null;
            }
            counts = new AtomicLong[buckets.length + 1];
        }
    }

    public Iterator<Metric> iterator() {

        roll();

        return new Iterator<Metric>() {

            int            i = 0;

            private Metric metric;

            @Override
            public boolean hasNext() {
                if (prevCounts == null) {
                    return false;
                }
                if (metric == null) {
                    while (i < prevCounts.length) {
                        if (prevCounts[i] != null) {
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

        Id   id;

        long count;

        public BucketMetric(int i) {
            String bucketTag = getBucketTag(i);
            id = LookoutBucketCounter.this.id().withTag(BUCKET_TAG_NAME, bucketTag);
            this.count = prevCounts[i].get();
        }

        @Override
        public Id id() {
            return id;
        }

        @Override
        public Indicator measure() {
            long now = Clock.SYSTEM.wallTime();
            Indicator indicator = new Indicator(now, id).addMeasurement(Statistic.buckets.name(),
                count);
            return indicator;
        }

    }
}