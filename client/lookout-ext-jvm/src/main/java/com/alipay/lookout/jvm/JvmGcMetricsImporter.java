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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class JvmGcMetricsImporter implements MetricsImporter {

    @Override
    public void register(Registry registry) {
        Id id = registry.createId("jvm.gc");
        MixinMetric mixin = registry.mixinMetric(id);

        mixin.gauge("young.count", new Gauge<Long>() {
            @Override
            public Long value() {
                GcInfo.refresh();
                return GcInfo.youngGCCount;
            }
        });
        mixin.gauge("young.time", new Gauge<Long>() {
            @Override
            public Long value() {
                GcInfo.refresh();
                return GcInfo.youngGCTime;
            }
        });
        mixin.gauge("old.count", new Gauge<Long>() {
            @Override
            public Long value() {
                GcInfo.refresh();
                return GcInfo.oldGCCount;
            }
        });
        mixin.gauge("old.time", new Gauge<Long>() {
            @Override
            public Long value() {
                GcInfo.refresh();
                return GcInfo.oldGCTime;
            }
        });
    }

    private static class GcInfo {
        static Map<String, GcType> m = new HashMap<String, GcType>();
        volatile static long       youngGCCount;
        volatile static long       youngGCTime;
        volatile static long       oldGCCount;
        volatile static long       oldGCTime;
        volatile static long       lastRefreshedTime;

        static {
            m.put("ConcurrentMarkSweep", GcType.OLD);
            m.put("Copy", GcType.YOUNG);
            m.put("G1 Old Generation", GcType.OLD);
            m.put("G1 Young Generation", GcType.YOUNG);
            m.put("MarkSweepCompact", GcType.OLD);
            m.put("PS MarkSweep", GcType.OLD);
            m.put("PS Scavenge", GcType.YOUNG);
            m.put("ParNew", GcType.YOUNG);
        }

        static synchronized void refresh() {
            if (System.currentTimeMillis() - lastRefreshedTime <= 1000) {//1s cache
                return;
            }
            List<GarbageCollectorMXBean> garbageCollectorMxBeans = ManagementFactory
                .getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMxBeans) {
                String name = garbageCollectorMXBean.getName();
                long gccount = garbageCollectorMXBean.getCollectionCount();
                long gctime = garbageCollectorMXBean.getCollectionTime();
                GcType type = m.get(name);
                switch (type) {
                    case YOUNG:
                        youngGCCount = gccount;
                        youngGCTime = gctime;
                        break;
                    case OLD:
                        oldGCCount = gccount;
                        oldGCTime = gctime;
                        break;
                }
            }
            lastRefreshedTime = System.currentTimeMillis();
        }
    }

    enum GcType {
        OLD, YOUNG
    }

}
