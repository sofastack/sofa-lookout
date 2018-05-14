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
package com.alipay.lookout.jvm;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.spi.MetricsImporter;

import java.lang.management.ManagementFactory;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class JvmThreadsMetricsImporter implements MetricsImporter {

    @Override
    public void register(Registry registry) {
        Id id = registry.createId("jvm.threads");
        MixinMetric mixin = registry.mixinMetric(id);

        mixin.gauge("peak", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return ManagementFactory.getThreadMXBean().getPeakThreadCount();
            }
        });
        mixin.gauge("daemon", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return ManagementFactory.getThreadMXBean().getDaemonThreadCount();
            }
        });
        mixin.gauge("totalStarted", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
            }
        });
        mixin.gauge("active", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return ManagementFactory.getThreadMXBean().getThreadCount();
            }
        });

    }
}
