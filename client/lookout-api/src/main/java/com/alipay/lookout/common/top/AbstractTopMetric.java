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
package com.alipay.lookout.common.top;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Created by kevin.luy@alipay.com on 2017/3/31.
 */
abstract class AbstractTopMetric {
    private volatile TreeSet<TopUtil.Entry<Id, Long>> set             = new TreeSet<TopUtil.Entry<Id, Long>>(
                                                                          comparator);
    private final int                                 maxNumber;
    private final Registry                            registry;
    protected final Id                                id;
    static final Comparator<TopUtil.Entry<Id, Long>>  comparator      = new Comparator<TopUtil.Entry<Id, Long>>() {
                                                                          @Override
                                                                          public int compare(TopUtil.Entry<Id, Long> o1,
                                                                                             TopUtil.Entry<Id, Long> o2) {
                                                                              return o1.getValue() > o2
                                                                                  .getValue() ? 1
                                                                                  : (o1.getValue() < o2
                                                                                      .getValue() ? -1
                                                                                      : 0);
                                                                          }
                                                                      };
    private final TopUtil.Order                       order;
    private long                                      lastRolledStamp = -1l;

    AbstractTopMetric(Registry registry, Id id, int maxNumber, TopUtil.Order order) {
        this.maxNumber = maxNumber;
        this.registry = registry;
        this.id = id;
        this.order = order;
    }

    protected void pushAsync(Long value, Id timerId) {
        final TopUtil.Entry<Id, Long> entry = new TopUtil.Entry<Id, Long>(timerId, value);
        TopUtil.executor.execute(new Runnable() {
            @Override
            public void run() {
                pushSafe(set, entry);
            }
        });
    }

    private void pushSafe(TreeSet<TopUtil.Entry<Id, Long>> set, TopUtil.Entry<Id, Long> e) {
        synchronized (set) {
            if (!set.isEmpty()) {
                boolean replaceable = false;
                TopUtil.Entry<Id, Long> boundaryTarget = null;
                if (order == TopUtil.Order.DESC) {
                    boundaryTarget = set.first();
                    replaceable = boundaryTarget.getValue() < e.getValue();
                } else {
                    boundaryTarget = set.last();
                    replaceable = boundaryTarget.getValue() > e.getValue();
                }
                if (replaceable & set.size() >= maxNumber) {
                    remove(set, boundaryTarget);
                }
                if (!replaceable & set.size() >= maxNumber) {
                    return;//不add
                }
            }
            add(set, e);
        }
    }

    private void remove(TreeSet<TopUtil.Entry<Id, Long>> set, TopUtil.Entry<Id, Long> boundaryTarget) {
        if (set.remove(boundaryTarget)) {
            //remove metric from registry
            registry.removeMetric(boundaryTarget.getKey());
        }
    }

    private void add(TreeSet<TopUtil.Entry<Id, Long>> set, TopUtil.Entry<Id, Long> e) {
        if (set.add(e)) {
            //ADD or get metric from registry
            getOrAddFromRegistry(registry, e);
        }
    }

    protected void getOrAddFromRegistry(Registry registry, final TopUtil.Entry<Id, Long> e) {
        registry.gauge(e.getKey(), new RollableTopGauge(this, e.getValue()));
    }

    /**
     * 不会并发poll所以不需要加锁（即使有并发重复roll也没关系）；
     *
     * @param rollStamp
     */
    public void roll(long rollStamp) {
        if (rollStamp > lastRolledStamp) {
            lastRolledStamp = rollStamp;
            set = new TreeSet<TopUtil.Entry<Id, Long>>(comparator);
        }
    }

    @Override
    public String toString() {
        return "Top_" + maxNumber + "_gauger@" + id;
    }
}
