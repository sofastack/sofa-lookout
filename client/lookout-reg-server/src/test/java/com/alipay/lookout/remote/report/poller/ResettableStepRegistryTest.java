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
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.LookoutCounter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/30
 */
public class ResettableStepRegistryTest {
    @Test
    public void test() {
        ResettableStepRegistry r = new ResettableStepRegistry(Clock.SYSTEM, new LookoutConfig(),
            1000L);
        assertThat(r.getCommonTagValue("foo")).isNull();
        r.setCommonTag("foo", "bar");
        assertThat(r.getCommonTagValue("foo")).isEqualTo("bar");
        assertThat(r.commonTags()).hasSize(1).containsEntry("foo", "bar");
        r.setCommonTag("foo", null);
        assertThat(r.commonTags()).isEmpty();
        r.setCommonTag("foo", "bar");
        assertThat(r.commonTags()).isNotEmpty();
        r.removeCommonTag("foo");
        assertThat(r.commonTags()).isEmpty();

        Id bazId = r.createId("baz");
        Counter bazCounter = r.counter(bazId);
        assertThat(bazCounter).isInstanceOf(LookoutCounter.class);
        assertThat(r.get(bazId)).isNotNull();
    }
}