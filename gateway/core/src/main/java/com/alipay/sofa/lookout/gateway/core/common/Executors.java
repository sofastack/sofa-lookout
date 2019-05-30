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
package com.alipay.sofa.lookout.gateway.core.common;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 对JDK的线程池进行包装, 添加监控线程池情况的功能
 * Created by kevin.luy@alipay.com on 2018/6/12.
 */
public final class Executors {
    private static final String METRIC_NAME = "threadpool.stats";

    private Executors() {
    }

    /**
     * 线程池 - 具有metrics统计*
     *
     * @param coreSize
     * @param maxSize
     * @param keepAliveMills
     * @param queueSize
     * @param poolName
     * @param registry
     * @return
     */
    public static ThreadPoolExecutor newThreadPoolExecutor(int coreSize, int maxSize, long keepAliveMills, int
            queueSize, String poolName, RejectedExecutionHandler rejectedExecutionHandler, Registry registry) {
        return newThreadPoolExecutor(coreSize, maxSize, keepAliveMills, poolName, rejectedExecutionHandler,
                new LinkedBlockingQueue<>(queueSize), registry);
    }

    public static ThreadPoolExecutor newFixedThreadPoolExecutor(int size, int
            queueSize, String poolName, RejectedExecutionHandler rejectedExecutionHandler, Registry registry) {
        return newThreadPoolExecutor(size, size, 0, poolName, rejectedExecutionHandler,
                new LinkedBlockingQueue<>(queueSize), registry);
    }

    public static ThreadPoolExecutor newFixedThreadPoolExecutor(int size, int queueSize, String poolName, ThreadFactory tf,
                                                                RejectedExecutionHandler rejectedExecutionHandler, Registry registry) {

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size,
                0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueSize),
                tf, rejectedExecutionHandler);

        // add metrics for thread pools
        Id id = registry.createId(METRIC_NAME)
                .withTag("priority", PRIORITY.HIGH.name())
                .withTag("name", poolName);

        MixinMetric mixinMetric = registry.mixinMetric(id);
        watch(executor, mixinMetric);

        return executor;
    }

    private static MixinMetric watch(ThreadPoolExecutor executor, MixinMetric mixinMetric) {
        mixinMetric.gauge("active.count", executor::getActiveCount);
        mixinMetric.gauge("queue.size", () -> executor.getQueue().size());
        mixinMetric.gauge("pool.size", executor::getPoolSize);
        mixinMetric.gauge("max.pool.size", executor::getMaximumPoolSize);
        mixinMetric.gauge("largest.pool.size", executor::getLargestPoolSize);
        return mixinMetric;
    }

    /**
     * 线程池 - 具有metrics统计*
     *
     * @param coreSize
     * @param maxSize
     * @param keepAliveMills
     * @param poolName
     * @param workQueue
     * @param registry
     * @return
     */
    public static ThreadPoolExecutor newThreadPoolExecutor(int coreSize,
                                                           int maxSize,
                                                           long keepAliveMills,
                                                           String poolName,
                                                           RejectedExecutionHandler rejectedExecutionHandler,
                                                           BlockingQueue<Runnable> workQueue,
                                                           Registry registry) {
        Preconditions.checkNotNull(poolName, "No thread pool name!");
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize,
            keepAliveMills <= 0 ? 0 : keepAliveMills, TimeUnit.MILLISECONDS, workQueue,
            getNamedThreadFactory(poolName), rejectedExecutionHandler);
        //add metrics for thread pools
        Id id = registry.createId(METRIC_NAME).withTag("priority", PRIORITY.HIGH.name())
            .withTag("name", poolName);
        MixinMetric mixinMetric = registry.mixinMetric(id);
        watch(executor, mixinMetric);
        return executor;
    }

    private static ThreadFactory getNamedThreadFactory(String poolName) {
        return new ThreadFactoryBuilder().setNameFormat(poolName + "-%d").build();
    }

}
