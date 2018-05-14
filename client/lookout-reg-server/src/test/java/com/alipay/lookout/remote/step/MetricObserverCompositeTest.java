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
package com.alipay.lookout.remote.step;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.MetricObserverMeasurementsFilter;
import com.alipay.lookout.report.MetricObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/8/19.
 */
public class MetricObserverCompositeTest {

    MockClock                clock = new MockClock();
    List<LookoutMeasurement> measurements;

    @Before
    public void init() {
        Registry registry = new DefaultRegistry();
        Id id = registry.createId("aaa");
        measurements = new ArrayList<LookoutMeasurement>();
        for (int i = 0; i < 10; i++) {
            LookoutMeasurement measurement = new LookoutMeasurement(new Date(), id);
            measurement.addTag("type" + i, "aa" + i);
            measurement.put("xxx" + i, i);
            measurement.put("yyy" + i, i);
            measurement.put("zzz" + i, i);
            measurements.add(measurement);
        }

        Id id2 = registry.createId("bbb");
        for (int i = 0; i < 10; i++) {
            LookoutMeasurement measurement = new LookoutMeasurement(new Date(), id2);
            measurement.addTag("type" + i, "aa" + i);
            measurement.put("ttt" + i, i);
            measurement.put("bbb" + i, i);
            measurements.add(measurement);
        }

    }

    @Test
    public void testMetricObserverOnlyEnable() {
        MetricObserverComposite metricObserverComposite = new MetricObserverComposite();

        MockMetricObserver metricObserver1 = new MockMetricObserver();
        MockMetricObserver metricObserver2 = new MockMetricObserver();
        metricObserverComposite.addMetricObserver(metricObserver1);
        metricObserverComposite.addMetricObserver(metricObserver2);
        metricObserver2.enable = false;
        if (metricObserverComposite.isEnable()) {
            metricObserverComposite.update(measurements, null);
        }
        Assert.assertEquals(20, metricObserver1.value);
        Assert.assertEquals(0, metricObserver2.value);

    }

    @Test
    public void testMetricObserverAllDisable() {
        MetricObserverComposite metricObserverComposite = new MetricObserverComposite();

        MockMetricObserver metricObserver1 = new MockMetricObserver();
        MockMetricObserver metricObserver2 = new MockMetricObserver();
        metricObserverComposite.addMetricObserver(metricObserver1);
        metricObserverComposite.addMetricObserver(metricObserver2);
        metricObserver1.enable = false;
        metricObserver2.enable = false;

        if (metricObserverComposite.isEnable()) {
            metricObserverComposite.update(measurements, null);
        }
        Assert.assertEquals(0, metricObserver1.value);
        Assert.assertEquals(0, metricObserver2.value);

    }

    @Test
    public void testMetricObserverMeasurmentsFilter() {

        class MockMetricObserver2 implements MetricObserver<LookoutMeasurement> {
            public boolean enable = true;
            public int     value  = 0;

            @Override
            public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
                value = measures.size();
            }

            @Override
            public boolean isEnable() {
                return enable;
            }
        }

        MetricObserverComposite metricObserverComposite = new MetricObserverComposite();

        MockMetricObserver metricObserver1 = new MockMetricObserver();
        MockMetricObserver2 metricObserver2 = new MockMetricObserver2();

        metricObserverComposite.addMetricObserver(metricObserver1);
        metricObserverComposite.addMetricObserver(metricObserver2);

        metricObserverComposite
            .addMetricObserverMeasurementsFilter(new MetricObserverMeasurementsFilter<LookoutMeasurement>() {
                @Override
                public List<LookoutMeasurement> filter(List<LookoutMeasurement> measurements,
                                                       MetricObserver metricObserver) {
                    List<LookoutMeasurement> lookoutMeasurementList = new ArrayList<LookoutMeasurement>();
                    if (metricObserver instanceof MockMetricObserver) {

                        for (LookoutMeasurement lookoutMeasurement : measurements) {
                            if (lookoutMeasurement.metricId().name().equalsIgnoreCase("bbb")) {
                                lookoutMeasurementList.add(lookoutMeasurement);
                            }
                        }
                        return lookoutMeasurementList;
                    }
                    return measurements;
                }
            });

        if (metricObserverComposite.isEnable()) {
            metricObserverComposite.update(measurements, null);
        }
        Assert.assertEquals(10, metricObserver1.value);
        Assert.assertEquals(20, metricObserver2.value);

    }

    class MockMetricObserver implements MetricObserver<LookoutMeasurement> {
        public boolean enable = true;
        public int     value  = 0;

        @Override
        public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
            value = measures.size();
        }

        @Override
        public boolean isEnable() {
            return enable;
        }
    }

}
