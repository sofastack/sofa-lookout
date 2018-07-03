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
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.spi.MetricsImporter;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class JvmMemoryMetricsImporter implements MetricsImporter {
    private static final Logger LOGGER          = LookoutLoggerFactory
                                                    .getLogger(JvmMemoryMetricsImporter.class);
    private static final String CODE_CACHE_NAME = "Code Cache";

    @Override
    public void register(Registry registry) {
        Id id = registry.createId("jvm.memory");
        MixinMetric mixin = registry.mixinMetric(id);
        heapImport(mixin);
        nonheapImport(mixin);
        codeCacheImport(mixin);
    }

    private void nonheapImport(MixinMetric mixin) {

        mixin.gauge("nonheap.init", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getInit();
            }
        });
        mixin.gauge("nonheap.used", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
            }
        });
        mixin.gauge("nonheap.committed", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
            }
        });
        mixin.gauge("nonheap.max", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax();
            }
        });
    }

    private void heapImport(MixinMetric mixin) {
        mixin.gauge("heap.init", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
            }
        });
        mixin.gauge("heap.used", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
            }
        });
        mixin.gauge("heap.committed", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
            }
        });
        mixin.gauge("heap.max", new Gauge<Long>() {
            @Override
            public Long value() {
                return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
            }
        });
    }

    private void codeCacheImport(MixinMetric mixin) {
        final MemoryPoolMXBean codeCacheMXBean = getCodeCacheMXBean();
        if (codeCacheMXBean == null) {
            LOGGER.info("can't get code cache MemoryPoolMXBean, won't report code cache usage.");
            return;
        }

        mixin.gauge("codecache.init", new Gauge<Long>() {
            @Override
            public Long value() {
                return codeCacheMXBean.getUsage().getInit();
            }
        });
        mixin.gauge("codecache.used", new Gauge<Long>() {
            @Override
            public Long value() {
                return codeCacheMXBean.getUsage().getUsed();
            }
        });
        mixin.gauge("codecache.committed", new Gauge<Long>() {
            @Override
            public Long value() {
                return codeCacheMXBean.getUsage().getCommitted();
            }
        });
        mixin.gauge("codecache.max", new Gauge<Long>() {
            @Override
            public Long value() {
                return codeCacheMXBean.getUsage().getMax();
            }
        });
    }

    private MemoryPoolMXBean getCodeCacheMXBean() {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            if (CODE_CACHE_NAME.equals(memoryPoolMXBean.getName())) {
                return memoryPoolMXBean;
            }
        }

        return null;
    }
}
