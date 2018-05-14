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
package com.alipay.lookout.core;

import com.alipay.lookout.MockId;
import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.report.filter.PriorityMetricFilter;
import org.junit.Assert;
import org.junit.Test;

import static com.alipay.lookout.common.LookoutConstants.TAG_PRIORITY_KEY;

/**
 * Created by kevin.luy@alipay.com on 2017/2/23.
 */
public class PriorityMetricFilterTest {

    @Test
    public void testPriorityMetricNoPriority() {
        PriorityMetricFilter filter1 = new PriorityMetricFilter(PRIORITY.HIGH);
        PriorityMetricFilter filter2 = new PriorityMetricFilter(PRIORITY.NORMAL);

        Assert.assertFalse(filter1.matches(new DefaultCounter(Clock.SYSTEM, new MockId("aa"))));
        Assert.assertTrue(filter2.matches(new DefaultCounter(Clock.SYSTEM, new MockId("aa"))));

    }

    @Test
    public void testPriorityMetricNormalPriority() {
        PriorityMetricFilter filter1 = new PriorityMetricFilter(PRIORITY.HIGH);
        PriorityMetricFilter filter2 = new PriorityMetricFilter(PRIORITY.NORMAL);

        Assert.assertFalse(filter1.matches(new DefaultCounter(Clock.SYSTEM, new MockId("aa")
            .withTag(TAG_PRIORITY_KEY, "XX"))));
        Assert.assertTrue(filter2.matches(new DefaultCounter(Clock.SYSTEM, new MockId("aa")
            .withTag(TAG_PRIORITY_KEY, "XX"))));

    }

    @Test
    public void testPriorityMetric() {
        PriorityMetricFilter filter1 = new PriorityMetricFilter(PRIORITY.HIGH);
        PriorityMetricFilter filter2 = new PriorityMetricFilter(PRIORITY.NORMAL);

        Assert.assertTrue(filter1.matches(new DefaultCounter(Clock.SYSTEM, new MockId("aa")
            .withTag(TAG_PRIORITY_KEY, PRIORITY.HIGH.name()))));
        Assert.assertTrue(filter2.matches(new DefaultCounter(Clock.SYSTEM, new MockId("aa")
            .withTag(TAG_PRIORITY_KEY, PRIORITY.NORMAL.name()))));

    }

}
