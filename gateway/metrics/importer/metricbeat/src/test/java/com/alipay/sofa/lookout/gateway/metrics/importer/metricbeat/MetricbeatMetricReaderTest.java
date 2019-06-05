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

import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/27
 */
public class MetricbeatMetricReaderTest {
    @Test
    public void testRead() {
        // TODO 这个测试用例里的 metricset beat 都会作为tags, 但name字段冲突了!

        String str = "{\"@timestamp\":\"2018-03-29T08:27:21.200Z\",\"metricset\":{\"name\":\"network\","
                     + ("\"module\":\"system\",\"rtt\":3487},\"system\":{\"network\":{\"in\":{\"errors\":0,\"dropped\":0,"
                        + "\"bytes\":0,\"packets\":0},\"out\":{\"errors\":0,\"dropped\":0,\"packets\":0,\"bytes\":0},"
                        + "\"name\":\"ip_vti0\"}},\"beat\":{\"name\":\"moby\",\"hostname\":\"moby\",\"version\":\"6.2.3\"}}");
        RawMetric rm = new RawMetric();
        rm.setRawBody(str.getBytes(StandardCharsets.UTF_8));
        List<Metric> list = new MetricbeatMetricReader().read(rm).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        for (Metric m : list) {
            sb.append(m.toString());
        }
        System.out.println(sb);
        Assert
            .assertTrue(sb
                .toString()
                .contains(
                    "Metric{name='system.network.out.bytes', value=0.0, timestamp=1522312041200, info='null', tags={name=moby, hostname=moby, version=6.2.3, module=system}}"));
    }
}