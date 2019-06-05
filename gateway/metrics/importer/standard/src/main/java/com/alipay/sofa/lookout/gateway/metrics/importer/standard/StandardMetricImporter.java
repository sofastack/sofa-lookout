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
package com.alipay.sofa.lookout.gateway.metrics.importer.standard;

import com.alipay.sofa.lookout.gateway.core.common.WebfluxUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricImporterUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.importer.AbstractWebfluxImporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetricHead;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.alipay.sofa.lookout.gateway.core.common.Constants.*;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
public class StandardMetricImporter extends AbstractWebfluxImporter {
    public static final String  SNAPPY           = "snappy";
    public static final String  CONTENT_ENCODING = "Content-Encoding";
    public static final String  WAIT_MINUTES     = "Wait-Minutes";
    private static final String ERR              = "Err";

    public StandardMetricImporter() {
        super("standard");
    }

    @Override
    protected Mono<ServerResponse> doHandle(ServerRequest request, RawMetric rm) {
        RawMetricHead head = rm.getHead();
        String clientIp = WebfluxUtils.getHeaderValue(request, CLIENT_IP_HEADER_NAME);
        String appName = WebfluxUtils.getHeaderValue(request, APP_HEADER_NAME);
        String priority = WebfluxUtils.getHeaderValue(request, PRIORITY_HEADER_NAME);
        if (clientIp != null) {
            head.setClientIp(clientIp);
        }
        head.setStandardAppName(appName);
        head.setStandardPriority(priority);

        String uri = request.uri().getPath();
        // 非lookout sdk
        if (uri.startsWith("/lookout/metrics")) {
            Map<String, String> extraTags = rm.getExtraTags();
            MetricImporterUtils.resolveExtraTagsFromURI("/lookout/metrics", uri, extraTags);
            try {
                MetricImporterUtils.validExtraTags(extraTags);
            } catch (IllegalArgumentException e) {
                return error(e.getMessage());
            }
            appName = extraTags.get("app");
        }

        // app check
        if (StringUtils.isEmpty(appName)) {
            return ServerResponse.status(HttpStatus.FORBIDDEN).header(WAIT_MINUTES, "5")
                .header(ERR, "APP_NAME_NULL").build();
        }

        // TODO 原先这里有 access control 黑名单的逻辑, 新版如果还需要的话可以做在filter里

        if (request.method() == HttpMethod.GET) {
            return ServerResponse.ok().syncBody("OK");
        }

        String contentEncoding = WebfluxUtils.getHeaderValue(request, CONTENT_ENCODING);

        head.setSnappy(SNAPPY.equals(contentEncoding));
        rm.setSourceType(SourceType.STANDARD);
        rm.setPushMode(true);

        super.fire(rm);

        return ServerResponse.ok().build();
    }
}
