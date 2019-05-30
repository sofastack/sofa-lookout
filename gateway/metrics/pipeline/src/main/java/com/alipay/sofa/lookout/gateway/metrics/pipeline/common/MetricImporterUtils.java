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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.common;

import com.alipay.sofa.lookout.gateway.core.common.WebfluxUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * utils for importer
 *
 * @author kevin.luy@antfin.com
 * @create 2018-11-30 12:03 PM
 **/
public final class MetricImporterUtils {

    private MetricImporterUtils() {
    }

    /**
     * TODO tags上指定的app, 和根据token反查出来的app, 在做统计的时候以哪个为准?
     * @param extraTags
     */
    public static void validExtraTags(Map<String, String> extraTags) {
        if (!extraTags.containsKey("app")) {
            throw new IllegalArgumentException("app tag in url is required");
        }
        if (!extraTags.containsKey("step")) {
            throw new IllegalArgumentException("step tag in url is required");
        }
        try {
            Integer.parseInt(extraTags.get("step"));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("step tag in url is invalid");
        }
    }

    /**
     * merge extra tags to the metric
     *
     * @param metric
     * @param extraTags
     * @return merged metric
     */
    public static Metric mergeWithExtraTags(Metric metric, Map<String, String> extraTags) {
        if (CollectionUtils.isEmpty(extraTags)) {
            return metric;
        }

        // 如果metric本身已经有的tag, 不要覆盖
        for (Map.Entry<String, String> e : extraTags.entrySet()) {
            metric.getTags().putIfAbsent(e.getKey(), e.getValue());
        }
        return metric;
    }

    /**
     * prometheus&standard&metric beat有一部分tags是放在url上, 而不是body里, 先称它做 external tags
     *
     * @param baseURIPath request bath URI
     * @param requestURI  importer push request URI.
     * @return extra tags (ref for java code style)
     */
    public static void resolveExtraTagsFromURI(String baseURIPath, String requestURI,
                                               Map<String, String> map) {
        Preconditions.checkNotNull(baseURIPath);
        Preconditions.checkNotNull(requestURI);
        // /*/metrics/{key1}/{value1}/{key2}/{value2}/...
        String subPath = requestURI.substring(baseURIPath.length());
        // 去掉末尾的/
        if (subPath.endsWith("/")) {
            subPath = subPath.substring(0, subPath.length() - 1);
        }

        String[] ss = StringUtils.split(subPath, '/');
        if (ss.length % 2 != 0) {
            throw new IllegalArgumentException("子路径必须有偶数个节");
        }
        for (int i = 0; i < ss.length; i += 2) {
            map.put(ss[i], ss[i + 1]);
        }
    }

    /**
     * resolve extra tags from request headers
     *
     * @param request
     * @param map
     * @return
     */
    public static Map<String, String> resolveExtraTagsFromRequestHeaders(ServerRequest request,
                                                                         Map<String, String> map) {
        Preconditions.checkNotNull(request);
        String app = WebfluxUtils.getHeaderValue(request, "app");
        if (app != null) {
            map.put("app", app);
        }

        String clientIp = WebfluxUtils.getHeaderValue(request, "ip");
        if (clientIp != null) {
            map.put("ip", clientIp);
        }

        String step = WebfluxUtils.getHeaderValue(request, "step");
        if (step != null) {
            map.put("step", step);
        }
        return map;
    }

}
