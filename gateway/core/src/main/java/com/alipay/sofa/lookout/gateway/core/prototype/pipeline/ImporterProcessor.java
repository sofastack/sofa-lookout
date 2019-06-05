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
package com.alipay.sofa.lookout.gateway.core.prototype.pipeline;

import com.alipay.sofa.lookout.gateway.core.prototype.importer.Importer;
import com.alipay.sofa.lookout.gateway.core.utils.ListUtils;

import java.util.List;

/**
 * 聚合多个importer即可, 伴随程序灭亡, 故不重写doStop(没意义)
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class ImporterProcessor<O> extends NoInputProcessor<Void, O> {
    private final List<Importer<O>> importers;

    public ImporterProcessor(List<Importer<O>> importers) {
        this.importers = ListUtils.unmodifiableList(importers);
    }

    protected void doStart() {
        super.doStart();
        for (Importer<O> importer : importers) {
            importer.start();
            importer.addConsumer(this::onOutput);
        }
    }
}
