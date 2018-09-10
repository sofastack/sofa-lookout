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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangzhuo
 * @version $Id: BucketDistributionSummary.java, v 0.1 2018年09月07日 上午11:47 zhangzhuo Exp $
 */
public abstract class BucketDistributionSummary implements DistributionSummary {

    private Registry registry;

    private long[] buckets;

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void enableBuckets(long[] buckets) {
        this.buckets = buckets;
    }

    protected void recordBucket(Id id, long amount) {
        if (buckets == null) {
            return;
        }
        int i = 0;
        for (; i < buckets.length; ++i) {
            if (amount <= buckets[i]) {
                break;
            }
        }
        String bucketTag = getBucketTag(i);
        Id bucketId = registry.createId(id.name() + "." + Statistic.buckets, id.tags()).withTag(
                BUCKET_TAG_NAME, bucketTag);
        Counter counter = registry.counter(bucketId);
        if (counter != null) {
            counter.inc();
        }
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

}