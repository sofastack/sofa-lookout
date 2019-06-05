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
package com.alipay.sofa.lookout.gateway.core.prototype.exporter.buffer;

import java.util.List;
import java.util.function.Consumer;

/**
 * TODO 可能需要配合定时器使用, 强制flush那些太久没有数据到来的buffer
 *
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public interface Buffer<T> {
    /**
     * 添加数据到buffer
     *
     * @param t
     */
    void add(T t);

    /**
     * 强制刷新
     */
    void flush();

    void setConsumer(Consumer<List<T>> consumer);
}
