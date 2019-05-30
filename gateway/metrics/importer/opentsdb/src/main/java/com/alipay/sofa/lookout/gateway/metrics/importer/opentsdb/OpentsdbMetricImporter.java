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
package com.alipay.sofa.lookout.gateway.metrics.importer.opentsdb;

import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricImporterUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.importer.AbstractWebfluxImporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
public class OpentsdbMetricImporter extends AbstractWebfluxImporter {
    public OpentsdbMetricImporter() {
        super("opentsdb");
    }

    @Override
    protected Mono<ServerResponse> doHandle(ServerRequest request, RawMetric rm) {
        Map<String, String> extraTags = rm.getExtraTags();
        MetricImporterUtils.resolveExtraTagsFromRequestHeaders(request, extraTags);
        try {
            MetricImporterUtils.validExtraTags(extraTags);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
        rm.setSourceType(SourceType.OPENTSDB);
        rm.setPushMode(true);
        super.fire(rm);

        return success();
    }
}
