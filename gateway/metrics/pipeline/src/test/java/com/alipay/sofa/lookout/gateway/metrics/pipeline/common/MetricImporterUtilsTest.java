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

import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 23:02
 **/
public class MetricImporterUtilsTest {

    @Test
    public void testValidExtraTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("app", "demo");
        tags.put("step", "10000");
        MetricImporterUtils.validExtraTags(tags);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidExtraTagsWithoutAppTag() {
        Map<String, String> tags = new HashMap<>();
        tags.put("step", "10000");
        MetricImporterUtils.validExtraTags(tags);
    }

    @Test
    public void testMergeWithExtraTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("app", "demo");
        tags.put("step", "10000");
        Metric metric = new Metric();
        metric.getTags().put("a", "b");
        Metric m = MetricImporterUtils.mergeWithExtraTags(metric, tags);
        Assert.assertEquals(3, m.getTags().size());
    }

    @Test
    public void testResolveExtraTagsFromURI() {
        Map<String, String> map = new HashMap<>();
        MetricImporterUtils.resolveExtraTagsFromURI("a/b/", "a/b/tk1/tv1/tk2/tv2"
                , map);

        Assert.assertEquals(2, map.size());
    }

    @Test
    public void testResolveExtraTagsFromRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("app", "demo");
        ServerRequest req = MockServerRequest.builder().headers(headers).build();
        Map<String, String> map = new HashMap<>();
        MetricImporterUtils.resolveExtraTagsFromRequestHeaders(req, map);
        Assert.assertEquals("demo", map.get("app"));
    }
}
