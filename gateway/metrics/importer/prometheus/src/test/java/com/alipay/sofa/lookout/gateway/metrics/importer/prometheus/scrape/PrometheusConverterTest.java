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

import com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.PrometheusConverter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 22:44
 **/
public class PrometheusConverterTest {
    @Test
    public void testConvertToModel() {
        Metric m = PrometheusConverter
            .convertToModel("cpu.user{instance_id=\"000001\", app=\"foo\"} 80.5");
        Assert.assertEquals(80.5, m.getValue(), 0.1);
        Assert.assertEquals("cpu.user", m.getName());
        Assert.assertEquals(-1, m.getTimestamp());
    }
}
