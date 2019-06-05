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
package com.alipay.sofa.lookout.gateway.core.prototype.exporter.buffer.impl;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>基于 线程本地 的buffer实现, 可以实现无锁. </p>
 * <p>但是有以下的缺点: 因为每个线程调用了之后都会为该线程创建一个buffer, 如果该线程调用add的频率不够高(比如调用10次之后就再也不调了), 那么这10次的数据就丢失了</p>
 * 如果可以保证: 总的线程数(使用到该buffer的)很少, 并且调用add的频率不低, 那么可以尝试使用这个类.
 *
 * @author kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public class ThreadLocalBuffer<T> extends AbstractBuffer<T> {
    private final ThreadLocal<Item> tl = ThreadLocal.withInitial(Item::new);
    private final int batch;
    private final long forceRefreshIntervalMills;

    public ThreadLocalBuffer(int batch, long forceRefreshIntervalMills) {
        this(batch, forceRefreshIntervalMills, emptyConsumer());
    }

    public ThreadLocalBuffer(int batch, long forceRefreshIntervalMills, Consumer<List<T>> consumer) {
        Preconditions.checkArgument(batch > 0 && batch <= 10000);
        Preconditions.checkArgument(forceRefreshIntervalMills > 0);
        this.batch = batch;
        this.consumer = Preconditions.checkNotNull(consumer);
        this.forceRefreshIntervalMills = forceRefreshIntervalMills;
    }

    @Override
    public void add(T t) {
        Item item = tl.get();
        List<T> list = item.list;
        list.add(t);
        if (list.size() >= batch || System.currentTimeMillis() > item.nextFlushTime) {
            flush(item);
        }
    }

    private void flush(Item item) {
        try {
            if (!item.list.isEmpty()) {
                consumer.accept(item.list);
            }
        } finally {
            item.list.clear();
            item.refresh(System.currentTimeMillis());
        }
    }

    @Override
    public void flush() {
        Item item = tl.get();
        flush(item);
    }

    public class Item {
        /**
         * buffer content
         */
        final List<T> list;

        long nextFlushTime;

        Item() {
            this.list = new ArrayList<>(batch);
            refresh(System.currentTimeMillis());
        }

        void refresh(long now) {
            this.nextFlushTime = now + forceRefreshIntervalMills;
        }
    }
}
