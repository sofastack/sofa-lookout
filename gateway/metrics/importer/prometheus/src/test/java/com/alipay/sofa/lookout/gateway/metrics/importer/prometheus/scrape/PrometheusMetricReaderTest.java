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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.scrape;

import com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.PrometheusMetricReader;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 22:51
 **/
public class PrometheusMetricReaderTest {

    @Test
    public void testRead() {
        RawMetric rawMetric = new RawMetric();
        rawMetric.setRawBody("cpu.user{instance_id=\"000001\", app=\"foo\"} 80.5".getBytes(Charset
            .forName("UTF-8")));
        PrometheusMetricReader reader = new PrometheusMetricReader();
        Stream<Metric> metricStream = reader.read(rawMetric);
        Metric m = metricStream.collect(Collectors.toList()).get(0);
        Assert.assertTrue(m.toString().contains("cpu.user"));
        Assert.assertTrue(m.toString().contains("80.5"));
        Assert.assertTrue(m.toString().contains("instance_id"));
        Assert.assertEquals(2, m.getTags().size());
        Assert.assertEquals(0, m.getTimestamp());

    }

    @Test
    public void testReadWithoutTags() {
        RawMetric rawMetric = new RawMetric();
        rawMetric.setRawBody("some_metric 42".getBytes(Charset.forName("UTF-8")));
        PrometheusMetricReader reader = new PrometheusMetricReader();
        Stream<Metric> metricStream = reader.read(rawMetric);
        Metric m = metricStream.collect(Collectors.toList()).get(0);
        Assert.assertTrue(m.toString().contains("some_metric"));
        Assert.assertTrue(m.toString().contains("42"));
        Assert.assertEquals(0, m.getTags().size());
        Assert.assertEquals(0, m.getTimestamp());

    }
}
