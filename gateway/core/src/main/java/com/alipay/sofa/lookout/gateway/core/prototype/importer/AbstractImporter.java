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

import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycleSupport;
import com.google.common.base.Preconditions;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public abstract class AbstractImporter<T> extends LifeCycleSupport implements Importer<T> {
    protected final CopyOnWriteArrayList<Consumer<T>> consumers = new CopyOnWriteArrayList<>();
    protected final String                            name;

    public AbstractImporter(String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void addConsumer(Consumer<T> consumer) {
        this.consumers.add(consumer);
    }

    protected void fire(T t) {
        for (Consumer<T> consumer : consumers) {
            consumer.accept(t);
        }
    }
}
