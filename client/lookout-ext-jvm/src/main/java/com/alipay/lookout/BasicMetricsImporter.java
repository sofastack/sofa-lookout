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
package com.alipay.lookout;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.spi.MetricsImporter;

import java.lang.management.ManagementFactory;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class BasicMetricsImporter implements MetricsImporter {
    private final long timestamp = System.currentTimeMillis();

    @Override
    public void register(Registry registry) {
        MixinMetric mixin = registry.mixinMetric(registry.createId("instance"));
        mixin.gauge("mem.total", new Gauge<Long>() {
            @Override
            public Long value() {
                return Runtime.getRuntime().totalMemory();
            }
        });

        mixin.gauge("mem.free", new Gauge<Long>() {
            @Override
            public Long value() {
                return Runtime.getRuntime().freeMemory();
            }
        });

        mixin.gauge("processors", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return Integer.valueOf(Runtime.getRuntime().availableProcessors());
            }
        });

        mixin.gauge("uptime", new Gauge<Long>() {
            @Override
            public Long value() {
                return Long.valueOf(System.currentTimeMillis() - timestamp);
            }
        });

        mixin.gauge("systemload.average", new Gauge<Double>() {
            @Override
            public Double value() {
                return Double.valueOf(ManagementFactory.getOperatingSystemMXBean()
                    .getSystemLoadAverage());
            }
        });
    }
}
