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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.importer;

import com.alipay.sofa.lookout.gateway.core.prototype.importer.Importer;
import com.alipay.sofa.lookout.gateway.core.prototype.pipeline.NoInputProcessor;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;

import java.util.Arrays;
import java.util.List;

/**
 * 内部聚合了多个importers
 *
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public class FakeMetricsImporterProcessor extends NoInputProcessor<Void, RawMetric> {
    private List<Importer<RawMetric>> importers;

    public FakeMetricsImporterProcessor() {
        importers = Arrays.asList(new FakeStandardMetricsImporter(),
            new FakePrometheusMetricsImporter());
    }

    @Override
    protected void doStart() {
        for (Importer<RawMetric> importer : importers) {
            importer.addConsumer(super::onOutput);
            importer.start();
        }
    }
}
