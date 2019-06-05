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

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.Timer;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.sofa.lookout.gateway.core.common.DataType;
import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycleSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/14
 */
public abstract class AbstractExporter<T> extends LifeCycleSupport implements Exporter<T> {
    protected final String   name;
    protected final Counter  count;
    protected final Counter  batch;
    protected final Counter  fail;
    protected final Timer    time;
    protected final Registry registry;

    public AbstractExporter(String name, Registry registry, DataType type) {
        this.name = name;
        this.registry = registry;
        Map<String, String> tags = new HashMap<>();
        tags.put("exporter", name);
        // 转成小写, 跟之前保持一致
        tags.put("type", type.name().toLowerCase());
        Id id = registry.createId("exporter.stats", tags);
        MixinMetric mm = registry.mixinMetric(id);

        // 统计导出个数(导出的metric条数)
        count = mm.counter("count");

        // 统计导出次数(每次会导出多条)
        batch = mm.counter("batch");

        // 统计失败次数
        fail = mm.counter("fail");

        // 统计请求耗时
        time = mm.timer("time");
    }

    @Override
    public String getName() {
        return name;
    }
}
