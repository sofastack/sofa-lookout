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

import com.alipay.sofa.lookout.gateway.core.prototype.importer.AbstractImporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public class FakeStandardMetricsImporter extends AbstractImporter<RawMetric> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FakeStandardMetricsImporter.class);

    public FakeStandardMetricsImporter() {
        super("FakeStandardMetricsImporter");
    }

    @Override
    protected void doStart() {
        Runnable runnable = () -> {
            for (int i = 0; i < 3600; i++) {
                RawMetric rm = new RawMetric();
                Map<String, String> tags = new HashMap<>();
                tags.put("key1", "value1");
                rm.setExtraTags(tags);
                rm.setRawBody("标准数据1|标准数据2".getBytes(StandardCharsets.UTF_8));
                rm.setPushMode(true);
                rm.setSourceType(SourceType.STANDARD);
                LOGGER.info("importer push一个数据 {}", rm);
                super.fire(rm);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("线程打断", e);
                }
            }
        };

        for (int i = 0; i < 4; i++) {
            new Thread(runnable).start();
        }
    }
}
