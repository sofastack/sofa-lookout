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
package com.alipay.lookout.starter.support.actuator;

import com.alipay.lookout.starter.base.AbstractTestBase;
import com.alipay.lookout.starter.support.reader.LookoutRegistryMetricReader;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.Metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * LookoutSpringBootMetricsImpl Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>2018/06/19</pre>
 */
public class LookoutSpringBootMetricsImplTest extends AbstractTestBase {

    @Autowired
    private LookoutSpringBootMetricsImpl lookoutSpringBootMetrics;

    @Autowired
    private CounterService               counterService;

    @Autowired
    private GaugeService                 gaugeService;

    @Autowired
    private LookoutRegistryMetricReader  lookoutRegistryMetricReader;

    /**
     * Method: testInstance
     */
    @Test
    public void testInstance() throws Exception {
        assertTrue(this.lookoutSpringBootMetrics != null);
        assertTrue(this.counterService != null);
        assertTrue(this.gaugeService != null);
        assertTrue(this.lookoutSpringBootMetrics == this.counterService);
        assertTrue(this.lookoutSpringBootMetrics == this.gaugeService);
    }

    /**
     * Method: increment(String metricName)
     * Method: decrement(String metricName)
     */
    @Test
    public void testIncrement() throws Exception {
        String metricName = "metricName";
        this.counterService.increment(metricName);
        Metric metric = this.lookoutRegistryMetricReader.findOne(metricName);
        assertTrue(metric != null);
        assertEquals(1L, metric.getValue());
        //decrement
        this.counterService.decrement(metricName);
        metric = this.lookoutRegistryMetricReader.findOne(metricName);
        assertTrue(metric != null);
        assertEquals(0L, metric.getValue());
    }

    /**
     * Method: reset(String metricName)
     */
    @Test
    public void testReset() throws Exception {
        String metricName = "metricName1";
        this.counterService.increment(metricName);
        Metric metric = this.lookoutRegistryMetricReader.findOne(metricName);
        assertTrue(metric != null);
        this.counterService.reset(metricName);
        assertTrue(this.lookoutRegistryMetricReader.findOne(metricName) == null);
    }

    /**
     * Method: submit(String metricName, double value)
     */
    @Test
    public void testSubmit() throws Exception {
        String gaugeName = "gaugeName";
        double value = 10;
        this.gaugeService.submit(gaugeName, value);
        //get
        Metric metric = this.lookoutRegistryMetricReader.findOne(gaugeName);
        assertEquals(value, metric.getValue());
    }
}
