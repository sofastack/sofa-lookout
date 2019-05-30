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
package com.alipay.sofa.lookout.gateway.core.prototype.queue;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.gateway.core.common.Executors;
import com.alipay.sofa.lookout.gateway.core.prototype.pipeline.AbstractProcessor;
import com.alipay.sofa.lookout.gateway.core.queue.MappedFilePersistentQueue;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 配合序列化器, 可以将这个类做成通用的, 从而提升到prototype里
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class BasePersistQueueProcessor<T> extends AbstractProcessor<T, T> {
    protected static final Logger     LOGGER                 = LoggerFactory
                                                                 .getLogger(BasePersistQueueProcessor.class);

    private static final String       QUEUE_SIZE_METRIC_NAME = "queue.size";
    private static final long         BYTES_OF_MB            = 1024 * 1024;

    private final int                 threads;
    private final String              dir;
    private final String              queueName;
    private final Registry            registry;
    private final Serializer<T>       serializer;
    private MappedFilePersistentQueue queue;
    private ExecutorService           executor;

    public BasePersistQueueProcessor(int threads, String dir, String queueName, Registry registry,
                                     Serializer<T> serializer) {
        Preconditions.checkArgument(threads > 0 && threads <= 100);
        this.threads = threads;
        this.dir = Preconditions.checkNotNull(dir);
        this.queueName = Preconditions.checkNotNull(queueName);
        this.registry = Preconditions.checkNotNull(registry);
        this.serializer = Preconditions.checkNotNull(serializer);
    }

    @Override
    protected void doStart() {
        try {
            queue = new MappedFilePersistentQueue(dir, queueName);
            Map<String, String> tags = new HashMap<>();
            tags.put("name", queueName);
            Id id = registry.createId(QUEUE_SIZE_METRIC_NAME, tags);
            registry.gauge(id, (Gauge<Number>) () -> queue.getBackFileSize() / BYTES_OF_MB);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ThreadFactory tf = new ThreadFactoryBuilder()
                .setNameFormat("queue_processor_poller-%d")
                .setThreadFactory(DequeueThread::new)
                .build();
        executor = Executors.newFixedThreadPoolExecutor(
                threads,
                100,
                "queue_processor_poller",
                tf,
                new ThreadPoolExecutor.AbortPolicy(),
                registry);

        // 启动线程
        for (int i = 0; i < threads; i++) {
            this.executor.execute(this::poll);
        }
    }

    private void poll() {
        Thread currentThread = Thread.currentThread();
        while (!currentThread.isInterrupted()) {
            try {
                byte[] data = queue.consume();
                if (data != null) {
                    T t = serializer.deserialize(data);
                    onOutput(t);
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                currentThread.interrupt();
                break;
            } catch (Exception e) {
                LOGGER.error("queue post process error", e);
            }
        }
    }

    @Override
    public void onInput(T t) {
        byte[] bytes = serializer.serialize(t);
        if (bytes != null) {
            queue.produce(bytes);
        }
    }
}
