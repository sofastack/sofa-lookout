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

import com.alipay.lookout.api.ManualClock;
import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/27
 */
public class MetricCacheTest {
    @Test
    public void test_clear() {
        ManualClock tc = new ManualClock();
        tc.setWallTime(1000);
        MetricCache mc = new MetricCache(tc, 1000, 4);
        mc.add(Collections.<MetricDto> emptyList());
        assertFalse(mc.getNextData(Collections.<Long> emptySet()).isEmpty());
        mc.clear();
        assertTrue(mc.getNextData(Collections.<Long> emptySet()).isEmpty());
    }

    @Test
    public void test_copy() {
        ManualClock tc = new ManualClock();
        tc.setWallTime(1000);
        MetricCache mc = new MetricCache(tc, 1000, 4);
        mc.add(Collections.<MetricDto> emptyList());
        MetricCache mc2 = new MetricCache(mc, 10, 10);
        assertNotNull(mc2.getNextData(Collections.<Long> emptySet()));
    }

    @Test
    public void test() {
        Set<Long> emptySet = Collections.emptySet();

        ManualClock tc = new ManualClock();
        tc.setWallTime(1000);

        MetricCache mc = new MetricCache(tc, 1000, 4);
        List<Slot> slots = mc.getNextData(emptySet);
        assertThat(slots).isEmpty();

        tc.setWallTime(2000);
        mc.add(Collections.<MetricDto> emptyList());
        slots = mc.getNextData(emptySet);
        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).getCursor()).isEqualTo(2000L);

        tc.setWallTime(3000);
        slots = mc.getNextData(emptySet);
        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).getCursor()).isEqualTo(2000L);

        mc.add(Collections.<MetricDto> emptyList());
        slots = mc.getNextData(emptySet);
        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getCursor()).isEqualTo(2000L);
        assertThat(slots.get(1).getCursor()).isEqualTo(3000L);

        tc.setWallTime(4000);
        mc.add(Collections.<MetricDto> emptyList());
        tc.setWallTime(5000);
        mc.add(Collections.<MetricDto> emptyList());

        slots = mc.getNextData(emptySet);
        assertThat(slots).hasSize(4);
        assertThat(slots.get(0).getCursor()).isEqualTo(2000L);
        assertThat(slots.get(1).getCursor()).isEqualTo(3000L);
        assertThat(slots.get(2).getCursor()).isEqualTo(4000L);
        assertThat(slots.get(3).getCursor()).isEqualTo(5000L);

        tc.setWallTime(6000);
        mc.add(Collections.<MetricDto> emptyList());

        slots = mc.getNextData(emptySet);
        assertThat(slots).hasSize(4);
        assertThat(slots.get(0).getCursor()).isEqualTo(6000L);
        assertThat(slots.get(1).getCursor()).isEqualTo(3000L);
        assertThat(slots.get(2).getCursor()).isEqualTo(4000L);
        assertThat(slots.get(3).getCursor()).isEqualTo(5000L);

        slots = mc.getNextData(Sets.newHashSet(3000L, 4000L));
        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getCursor()).isEqualTo(6000L);
        assertThat(slots.get(1).getCursor()).isEqualTo(5000L);

        slots = mc.getNextData(Sets.newHashSet(5000L, 6000L));
        assertThat(slots).isEmpty();
    }
}