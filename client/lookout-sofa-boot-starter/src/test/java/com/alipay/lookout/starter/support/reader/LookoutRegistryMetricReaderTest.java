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
package com.alipay.lookout.starter.support.reader;

import com.alipay.lookout.starter.base.AbstractTestBase;
import com.alipay.lookout.starter.support.actuator.LookoutSpringBootMetricsImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.MetricReaderPublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * LookoutRegistryMetricReader Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>2018/06/15</pre>
 */
@ActiveProfiles("reader")
public class LookoutRegistryMetricReaderTest extends AbstractTestBase {

    @Autowired
    private List<MetricReaderPublicMetrics> metricReaderPublicMetrics   = null;

    @Autowired
    private LookoutRegistryMetricReader     lookoutRegistryMetricReader = null;

    /**
     * Method: findOne(String metricName)
     */
    @Test
    public void testFindOne() throws Exception {
        assertNull(lookoutRegistryMetricReader.findOne(""));
        assertTrue(this.metricReaderPublicMetrics != null
                   && this.metricReaderPublicMetrics.size() > 0);
        assertNotNull(testRestTemplate);
        String endpointId = "mappings";
        String restUrl = urlHttpPrefix + "/" + endpointId;
        ResponseEntity<String> response = testRestTemplate.getForEntity(restUrl, String.class);
        assertTrue(StringUtils.isNotBlank(response.getBody()));
        //
        Metric metric = this.lookoutRegistryMetricReader
            .findOne(LookoutSpringBootMetricsImpl.LOOKOUT_GAUGE_PREFIX + "response." + endpointId);
        assertTrue(metric != null);
    }

    /**
     * Method: findAll()
     */
    @Test
    public void testFindAll() throws Exception {
        Iterable<Metric<?>> metricIterable = this.lookoutRegistryMetricReader.findAll();
        List<Metric> metricList = new ArrayList<Metric>();
        for (Metric<?> metric : metricIterable) {
            metricList.add(metric);
        }
        assertTrue(metricList.size() > 0);
    }

    /**
     * Method: count()
     */
    @Test
    public void testCount() throws Exception {
        long count = this.lookoutRegistryMetricReader.count();
        assertTrue(count > 0);
    }
}
