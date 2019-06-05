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
 * 基于synchronized关键字实现的buffer
 *
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public class LockBuffer<T> extends AbstractBuffer<T> {
    private final Object lock = new Object();
    private final int    batch;
    private List<T>      buffer;
    private long         nextFlushTime;
    private final long   forceRefreshIntervalMills;

    public LockBuffer(int batch, long forceRefreshIntervalMills) {
        this(batch, forceRefreshIntervalMills, emptyConsumer());
    }

    public LockBuffer(int batch, long forceRefreshIntervalMills, Consumer<List<T>> consumer) {
        Preconditions.checkArgument(batch > 0 && batch <= 10000);
        Preconditions.checkArgument(forceRefreshIntervalMills > 0);
        this.batch = batch;
        this.forceRefreshIntervalMills = forceRefreshIntervalMills;
        this.consumer = Preconditions.checkNotNull(consumer);
        this.buffer = new ArrayList<>(batch);
    }

    @Override
    public void add(T t) {
        List<T> f = null;
        long now = System.currentTimeMillis();
        synchronized (lock) {
            List<T> b = buffer;
            b.add(t);
            if (b.size() >= batch || now > nextFlushTime) {
                f = b;
                buffer = new ArrayList<>(batch);
                nextFlushTime = now + forceRefreshIntervalMills;
            }
        }
        if (f != null) {
            consumer.accept(f);
        }
    }

    @Override
    public void flush() {
        List<T> f;
        synchronized (lock) {
            f = buffer;
            buffer = new ArrayList<>(batch);
        }
        if (!f.isEmpty()) {
            consumer.accept(f);
        }
    }
}
