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
package com.alipay.lookout.common;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.LogObserver;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class MetricObserverUtilTest {

    @Test
    public void testAddMetricObserver() {
        LookoutRegistry reg = new LookoutRegistry(new LookoutConfig());
        LogObserver logObserver = new LogObserver();
        MetricObserverUtil.addMetricObservers(reg, logObserver);
        Assert.assertTrue(reg.getMetricObservers().contains(logObserver));
    }

    @Test
    public void testAddMetricObserverWithCompositeRegistry() {
        LookoutRegistry reg = new LookoutRegistry(new LookoutConfig());
        LogObserver logObserver = new LogObserver();
        CompositeRegistry compositeRegistry = new CompositeRegistry(Clock.SYSTEM);
        compositeRegistry.add(reg);
        MetricObserverUtil.addMetricObservers(compositeRegistry, logObserver);
        Assert.assertTrue(reg.getMetricObservers().contains(logObserver));
    }
}
