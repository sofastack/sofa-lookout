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
package com.alipay.lookout.remote.report.poller;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/30
 */
public class PollerControllerTest {
    static PollerController controller;

    @BeforeClass
    public static void init() {
        LookoutConfig config = new LookoutConfig();
        LookoutRegistry r = new LookoutRegistry(Clock.SYSTEM, null, config, null, 1000L);

        r.counter(r.createId("foo"));

        controller = new PollerController(r, 10);

    }

    @Test
    public void test() throws Exception {

        try {
            Set<Long> success = new HashSet<Long>();
            // sleep 200ms 错开时间 防止定时器偶然的时间误差
            Thread.sleep(200);
            // trigger
            List<Slot> data = controller.getNextData(success);
            assertThat(data).isEmpty();
            Thread.sleep(1000);
            data = controller.getNextData(success);
            assertThat(data).hasSize(1);
            success = extractCursors(data);
            data = controller.getNextData(success);
            assertThat(data).isEmpty();
            Thread.sleep(1000);

            success = Collections.emptySet();
            data = controller.getNextData(success);
            assertThat(data).hasSize(1);

            Thread.sleep(2000);

            data = controller.getNextData(Collections.<Long>emptySet());
            assertThat(data).hasSize(3);

            success = extractCursors(data);
            data = controller.getNextData(success);
            assertThat(data).isEmpty();

        } finally {
            controller.close();
        }

    }

    public void testGetConfig() {
        Assert.assertNotNull(controller.getMetricConfig());
    }

    private Set<Long> extractCursors(List<Slot> data) {
        Set<Long> set = Sets.newHashSetWithExpectedSize(data.size());
        for (Slot s : data) {
            set.add(s.getCursor());
        }
        return set;
    }
}