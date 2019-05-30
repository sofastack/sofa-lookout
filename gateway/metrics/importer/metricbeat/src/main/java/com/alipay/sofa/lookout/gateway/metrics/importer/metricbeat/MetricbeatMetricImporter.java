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
package com.alipay.sofa.lookout.gateway.metrics.importer.metricbeat;

import com.alipay.sofa.lookout.gateway.metrics.pipeline.importer.AbstractWebfluxImporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/16
 */
public class MetricbeatMetricImporter extends AbstractWebfluxImporter {
    private static final String BULK_RESPONSE_BODY = "{\"took\":10,\"errors\":false,\"items\":[]}";

    public MetricbeatMetricImporter() {
        super("metricbeat");
    }

    @Override
    protected Mono<ServerResponse> doHandle(ServerRequest request, RawMetric rm) {
        rm.setPushMode(true);
        rm.setSourceType(SourceType.METRICBEAT);
        super.fire(rm);
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(BULK_RESPONSE_BODY), String.class);
    }

    /**
     * Disguised as a es, mock the metadata request
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> getMetadata(ServerRequest request) {
        String body = "{\"name\":\"lookout\",\"cluster_name\":\"lookout-gateway\",\"cluster_uuid\":\"lKdNC-gqS-meA2ALMl0_4w\","
                      + "\"version\":{\"number\":\"5.3.2\",\"build_hash\":\"3068195\","
                      + "\"build_date\":\"2017-04-24T16:15:59.481Z\",\"build_snapshot\":false,"
                      + "\"lucene_version\":\"6.4.2\"},\"tagline\":\"You Know, for Search\"}";
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).syncBody(body);
    }

    /**
     * Disguised as a es, mock the beat template request
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> headBeatTemplate(ServerRequest request) {
        // 直接返回空即可
        return success();
    }
}
