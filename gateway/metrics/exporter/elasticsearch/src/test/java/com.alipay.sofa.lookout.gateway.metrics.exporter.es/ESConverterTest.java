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
package com.alipay.lookout.gateway.metric.exporter.es;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.lookout.gateway.metrics.exporter.es.ESConverter;
import com.alipay.sofa.lookout.gateway.metrics.exporter.es.ESEntity;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-22 18:05
 **/
public class ESConverterTest {

    @Test
    public void testConvert2ESModel() {
        Metric metric = new Metric();
        metric.setTimestamp(System.currentTimeMillis());
        metric.setValue(12l);
        metric.setName("jvm.memory.free");
        Map<String, String> tags = new HashMap<>();
        metric.setTags(tags);
        tags.put("k1", "v1");
        tags.put("k2", "v2");
        ESEntity e = ESConverter.toEsEntity(metric);
        String json = JSON.toJSONString(e);
        System.out.println(json);
        Assert.assertTrue(json.contains("[\"k1=v1\",\"k2=v2\"]"));
    }

    @Test
    public void testConvert2ESModelWithNoTags() {
        Metric metric = new Metric();
        metric.setTimestamp(System.currentTimeMillis());
        metric.setValue(12l);
        metric.setName("jvm.memory.free");
        Map<String, String> tags = new HashMap<>();
        metric.setTags(tags);
        ESEntity e = ESConverter.toEsEntity(metric);
        String json = JSON.toJSONString(e);
        System.out.println(json);
        Assert.assertFalse(json.contains("tags"));
    }
}
