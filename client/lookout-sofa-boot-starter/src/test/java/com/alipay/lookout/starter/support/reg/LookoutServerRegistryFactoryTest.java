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
package com.alipay.lookout.starter.support.reg;

import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.LogObserver;
import com.alipay.lookout.report.MetricObserver;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2018/5/23.
 */
public class LookoutServerRegistryFactoryTest {

    @Test
    public void testLookoutServerRegistryFactory() {
        MetricObserver<LookoutMeasurement> logObserver = new LogObserver();
        LookoutServerRegistryFactory factory = new LookoutServerRegistryFactory(
            Lists.newArrayList(logObserver));
        LookoutConfig config = new LookoutConfig();
        LookoutRegistry r = factory.get(config);
        Assert.assertEquals(2, r.getMetricObservers().size());
    }
}
