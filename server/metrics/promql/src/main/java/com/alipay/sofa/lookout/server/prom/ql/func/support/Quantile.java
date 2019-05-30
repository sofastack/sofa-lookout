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
package com.alipay.sofa.lookout.server.prom.ql.func.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author kevin.luy@antfin.com
 * @create 2018-11-30 11:17 PM
 **/
public class Quantile {

    public static double bucketQuantile(double q, List<PromBucket> buckets) {
        if (q < 0)
            return Float.NEGATIVE_INFINITY;
        if (q > 1)
            return Float.POSITIVE_INFINITY;

        if (buckets.size() == 2) {
            return Float.NaN;
        }
        Collections.sort(buckets, Comparator.comparingDouble(PromBucket::getUpperBound));
        ensureMonotonic(buckets);
        double rank = q * buckets.get(buckets.size() - 1).getCount();
        int i = 0;
        for (i = 0; i < buckets.size(); i++) {
            if (buckets.get(i).getCount() >= rank) {
                break;
            }
        }
        if (i == buckets.size() - 1) {
            return buckets.get(buckets.size() - 2).getUpperBound();
        }
        if (i == 0 && buckets.get(0).getUpperBound() <= 0) {
            return buckets.get(0).getUpperBound();
        }

        double bucketStart = 0f;
        double bucketEnd = buckets.get(i).getUpperBound();
        long count = buckets.get(i).getCount();

        if (i > 0) {
            bucketStart = buckets.get(i - 1).getUpperBound();
            count -= buckets.get(i - 1).getCount();
            rank -= buckets.get(i - 1).getCount();
        }
        return bucketStart + (bucketEnd - bucketStart) * (rank / count);
    }

    //保证单调性
    public static void ensureMonotonic(List<PromBucket> buckets) {
        long max = buckets.get(0).getCount();
        for (int i = 1; i < buckets.size(); i++) {
            if (buckets.get(i).getCount() > max) {
                max = buckets.get(i).getCount();
            } else {
                buckets.get(i).setCount(max);
            }
        }
    }

    /**
     * ====================for lookout sdk=======================
     */

    public static Bucket buildBucket(String bucketLabelValue, long count) {
        String[] str = StringUtils.split(bucketLabelValue, '-');
        long lowerBound = Long.parseLong(str[0]);
        long upperBound;
        if (str.length == 2) {
            upperBound = Long.parseLong(str[1]);
        } else {
            upperBound = Long.MAX_VALUE;
        }
        return new Bucket(lowerBound, upperBound, count);
    }

    public static double quantile(double q, List<Bucket> buckets) {
        if (q < 0 || q > 1) {
            throw new IllegalArgumentException("invalid quantile:" + q);
        }
        Collections.sort(buckets, Comparator.comparingLong(Bucket::getLowerBound));
        long sum = 0;
        for (Bucket bucket : buckets) {
            sum += bucket.getCount();
            bucket.setSum(sum);
        }
        double rank = buckets.get(buckets.size() - 1).getSum() * q;
        int i = 0;
        long prevSum = 0;
        for (i = 0; i < buckets.size(); i++) {
            sum = buckets.get(i).getSum();
            if (sum >= rank) {
                break;
            } else {
                prevSum = sum;
            }
        }
        long lowerBound = buckets.get(i).getLowerBound();
        long upperBound = buckets.get(i).getUpperBound();
        if (upperBound != Long.MAX_VALUE) {
            return lowerBound + (upperBound - lowerBound) * (rank - prevSum) / buckets.get(i).getCount();
        } else {
            return lowerBound;
        }
    }
}
