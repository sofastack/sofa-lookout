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
package com.alipay.sofa.lookout.gateway.core.prototype.exporter;

import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.gateway.core.common.DataType;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.buffer.Buffer;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.buffer.impl.LockBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 该抽象类的实现无需埋点, 埋点已经在这个类做了. 但又一个情况例外, 就是失败时不抛异常的情况需要自己埋fail的点
 *
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public abstract class AbstractBatchExporter<T> extends AbstractExporter<T> {
    private final Logger      LOGGER = LoggerFactory.getLogger(getClass());

    protected final Buffer<T> buffer;

    public AbstractBatchExporter(String name, Registry registry, DataType type) {
        this(name, registry, 100, type);
    }

    public AbstractBatchExporter(String name, Registry registry, int batch, DataType type) {
        super(name, registry, type);
        // 先用最简单的基于锁的实现, 因为在锁内做得事情非常少, 也非常快, 所以速度不会拖慢太多
        this.buffer = new LockBuffer<>(batch, 1000, this::flushList0);
    }

    /**
     * 批量刷新的一个包装, 添加埋点信息统计
     *
     * @param list
     */
    protected void flushList0(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        batch.inc();
        long begin = System.currentTimeMillis();
        try {
            boolean success = this.flushList(list);
            if (success) {
                long end = System.currentTimeMillis();
                count.inc(list.size());
                time.record(end - begin, TimeUnit.MILLISECONDS);
            } else {
                // 如果刷新失败, 则记录fail
                fail.inc(list.size());
            }
        } catch (Exception e) {
            // 抛异常也算是一种失败, 记录fail
            fail.inc(list.size());
            LOGGER.warn("exporter fail {} {}:{}", getName(), e.getClass().getSimpleName(),
                e.getMessage());
        }
    }

    /**
     * 批量刷新
     *
     * @param list
     * @return 刷新是否成功
     */
    protected abstract boolean flushList(List<T> list) throws Exception;

    @Override
    public void export(T t) {
        buffer.add(t);
    }
}