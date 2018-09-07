/**
 * Alipay.com Inc. Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.lookout.api;

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
            if (amount < buckets[i]) {
                break;
            }
        }
        String bucketTag = i < buckets.length ? String.valueOf(buckets[i]) : INFINITY;
        Id bucketId = id.withTag(BUCKET_TAG_NAME, bucketTag);
        Counter counter = registry.counter(bucketId);
        if (counter != null) {
            counter.inc();
        }
    }

}