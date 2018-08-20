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
import com.alipay.lookout.api.NoopRegistry;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.jdk8.Function;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kevin.luy@alipay.com on 2017/3/31.
 */
public final class TopUtil {

    private static final String                   TOP_NUM_TAG_KEY    = "n";

    static Executor                               executor           = new ThreadPoolExecutor(
                                                                         Runtime.getRuntime()
                                                                             .availableProcessors(),
                                                                         Runtime.getRuntime()
                                                                             .availableProcessors(),
                                                                         0L,
                                                                         TimeUnit.MILLISECONDS,
                                                                         new LinkedBlockingQueue<Runnable>(
                                                                             10000),
                                                                         getNamedThreadFactory(),
                                                                         new ThreadPoolExecutor.DiscardPolicy()); //TODO ADD lookout log?

    static final ConcurrentHashMap<Id, TopGauger> cache              = new ConcurrentHashMap<Id, TopGauger>();
    //防止用户使用失误，导致cache过多；
    static final int                              MAX_TOP_CACHE_SIZE = 200;

    private TopUtil() {
    }

    private static ThreadFactory getNamedThreadFactory() {
        return new DefaultThreadFactory("top-metrics-pool", true);
    }

    /**
     * 新创建一个 TopGuager 实例，不用重复创建相同的 TopGuager，需要复用；
     * 「默认降序」;
     *
     * @param registry  metric registry
     * @param id        metric id
     * @param maxNumber top 5/10/20...
     * @return TopGauger
     */
    public static TopGauger topGauger(final Registry registry, final Id id, final int maxNumber) {
        return topGauger(registry, id, maxNumber, Order.DESC);
    }

    public static TopGauger topGauger(final Registry registry, final Id id, final int maxNumber,
                                      final Order order) {
        if (registry instanceof NoopRegistry || (id == NoopRegistry.INSTANCE.createId(null))) {
            return NoopTopGauger.INSTANCE;
        }
        Id key = id.withTag(TOP_NUM_TAG_KEY, String.valueOf(maxNumber));
        TopGauger topGauger = computeIfAbsent(cache, key, new Function<Object, TopGauger>() {
            @Override
            public TopGauger apply(Object obj) {
                return new DefaultTopGauger(registry, id, maxNumber, order);
            }
        });
        return topGauger == null ? NoopTopGauger.INSTANCE : topGauger;
    }

    private static <E, T> T computeIfAbsent(ConcurrentHashMap<E, T> map, E key, Function<?, T> f) {
        T m = map.get(key);
        if (m == null) {
            if (map.size() > MAX_TOP_CACHE_SIZE) {
                return null;
            }
            T tmp = f.apply(null);
            m = map.putIfAbsent(key, tmp);
            if (m == null) {
                //first register
                m = tmp;
            }
        }
        return (T) m;
    }

    public enum Order {
        DESC, ASC
    }

    /**
     * 零时对象
     *
     * @param <K>
     * @param <V>
     */
    static class Entry<K, V>

    {
        private final K key;
        private V       value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry) o;
            return eq(key, e.getKey());
        }

        private static boolean eq(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }

        public int hashCode() {
            return (key == null ? 0 : key.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }

    }

    /**
     * 自定义线程工厂类
     */
    private static class DefaultThreadFactory implements ThreadFactory {

        //工厂复用时友好
        private static final AtomicInteger poolId = new AtomicInteger();

        private final AtomicInteger        nextId = new AtomicInteger();
        private final String               prefix;
        private final boolean              daemon;
        private final int                  priority;

        public DefaultThreadFactory(String poolName) {
            this(poolName, false, Thread.NORM_PRIORITY);
        }

        public DefaultThreadFactory(String poolName, boolean daemon) {
            this(poolName, daemon, Thread.NORM_PRIORITY);
        }

        public DefaultThreadFactory(String poolName, int priority) {
            this(poolName, false, priority);
        }

        public DefaultThreadFactory(String poolName, boolean daemon, int priority) {
            if (poolName == null) {
                throw new NullPointerException("poolName");
            }

            if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
                throw new IllegalArgumentException(
                    "priority: " + priority
                            + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
            }

            prefix = poolName + '-' + poolId.incrementAndGet() + '-';
            this.daemon = daemon;
            this.priority = priority;
            //            this.threadGroup = threadGroup;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + nextId.incrementAndGet());
            try {
                if (t.isDaemon()) {
                    if (!daemon) {
                        t.setDaemon(false);
                    }
                } else {
                    if (daemon) {
                        t.setDaemon(true);
                    }
                }

                if (t.getPriority() != priority) {
                    t.setPriority(priority);
                }
            } catch (Exception ignored) {
                // Doesn't matter even if failed to set.
            }
            return t;
        }

    }
}
