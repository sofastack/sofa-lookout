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

import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 22:32
 **/
public class OpentsdbMetricReaderTest {

    @Test
    public void testRead() throws UnsupportedEncodingException {
        OpentsdbMetricReader reader = new OpentsdbMetricReader();

        RawMetric metric = new RawMetric();
        metric
            .setRawBody("{\"metric\":\"jvm.mem.free\",\"value\":1.2,\"timestamp\":1557844146565,\"tags\":{\"app\":\"demo\"}}"
                .getBytes("UTF-8"));
        Stream<Metric> metricStream = reader.read(metric);
        Metric m = metricStream.collect(Collectors.toList()).get(0);
        Assert.assertTrue(m.toString().contains("jvm.mem.free"));
    }
}
