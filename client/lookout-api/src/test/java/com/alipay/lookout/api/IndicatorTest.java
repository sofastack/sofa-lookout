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
package com.alipay.lookout.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2018/5/16.
 */
public class IndicatorTest {

    @Test
    public void testAddMeasument() {
        Indicator indicator = new Indicator(11l, NoopRegistry.INSTANCE.createId("name"));
        indicator.addMeasurement("k", 1);
        indicator.addMeasurement(new Measurement("K2", "V2"));
        Assert.assertEquals(2, indicator.measurements().size());
        Assert.assertEquals(11l, indicator.getTimestamp());
        Assert.assertTrue(indicator.id() == NoopId.INSTANCE);

    }
}
