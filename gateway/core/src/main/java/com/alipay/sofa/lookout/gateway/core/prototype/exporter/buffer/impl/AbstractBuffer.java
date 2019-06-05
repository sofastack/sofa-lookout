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

import com.alipay.sofa.lookout.gateway.core.prototype.exporter.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Buffer的基类实现, 提供空Consumer, 和Consumer字段
 *
 * @author xiangfeng.xzc
 * @date 2018/12/3
 */
public abstract class AbstractBuffer<T> implements Buffer<T> {
    protected static final Logger      LOGGER         = LoggerFactory
                                                          .getLogger(AbstractBuffer.class);

    protected static final Consumer<?> EMPTY_CONSUMER = new Consumer<Object>() {
                                                          @Override
                                                          public void accept(Object list) {
                                                              LOGGER.warn(
                                                                  "{} no consumer is configured!",
                                                                  this);
                                                          }
                                                      };

    protected static <T> Consumer<List<T>> emptyConsumer() {
        return (Consumer<List<T>>) EMPTY_CONSUMER;
    }

    protected Consumer<List<T>> consumer = emptyConsumer();

    @Override
    public void setConsumer(Consumer<List<T>> consumer) {
        this.consumer = consumer;
    }

}
