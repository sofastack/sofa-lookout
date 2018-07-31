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

import com.alipay.lookout.api.ManualClock;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.report.MetricObserver;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/2/7.
 */
public class LookoutRegistryTest {

    static List<LookoutMeasurement> measurements;

    static LookoutRegistry          registry;
    static ManualClock              clock          = new ManualClock();
    static MetricObserver           metricObserver = new MetricObserver<LookoutMeasurement>() {

                                                       @Override
                                                       public boolean isEnable() {
                                                           return true;
                                                       }

                                                       @Override
                                                       public void update(List<LookoutMeasurement> measures,
                                                                          Map<String, String> metadata) {
                                                           System.out.println(measures.toString());
                                                       }
                                                   };

    //因为有限制，全局只能有一个 LookoutRegistry 实例
    static {
        registry = new LookoutRegistry(clock, metricObserver, new LookoutConfig());
    }

    @Test
    public void testAddMetricObservers() throws InterruptedException {
        registry.addMetricObserver(new MetricObserver() {
            @Override
            public void update(List measures, Map metadata) {
                System.out.println("===>2");
            }

            @Override
            public boolean isEnable() {
                return true;
            }
        });

        try {
            Field field = registry.getClass().getDeclaredField("metricObserverComposite");
            field.setAccessible(true);
            MetricObserverComposite metricObserverComposite = (MetricObserverComposite) field
                .get(registry);
            //do assert
            Assert.assertTrue(metricObserverComposite.size() > 1);
        } catch (Throwable e) {
            throw new RuntimeException("xx", e);
        }
    }

    static class LogObserver implements MetricObserver<LookoutMeasurement> {
        @Override
        public boolean isEnable() {
            return true;
        }

        @Override
        public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
            System.out.println(measures);
        }
    }

}
