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
package com.alipay.sofa.lookout.gateway.core.prototype.importer;

import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycle;

import java.util.function.Consumer;

/**
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public interface Importer<T> extends LifeCycle {
    /**
     * 返回该importer的名字, 做统计用
     * @return
     */
    String name();

    /**
     * 添加一个消费者监听该importer
     *
     * @param consumer
     */
    void addConsumer(Consumer<T> consumer);
}
