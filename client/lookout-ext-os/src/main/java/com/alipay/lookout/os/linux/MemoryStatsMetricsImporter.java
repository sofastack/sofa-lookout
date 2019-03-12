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
package com.alipay.lookout.os.linux;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.os.CachedMetricsImporter;
import com.alipay.lookout.os.utils.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * resolve memory metrics from "/proc/meminfo"
 * @author: kevin.luy@antfin.com
 * @create: 2019-03-12 17:30
 **/
public class MemoryStatsMetricsImporter extends CachedMetricsImporter {

    private final Logger        logger            = LookoutLoggerFactory
                                                      .getLogger(DiskUsageMetricsImporter.class);

    private static final String DEFAULT_FILE_PATH = "/proc/meminfo";
    private static final String SPLIT             = "\\s+";
    private String              filePath;
    private Memstats            memstats;

    /**
     * default constructor
     */
    public MemoryStatsMetricsImporter() {
        this(DEFAULT_FILE_PATH, DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * @param filePath
     * @param timeout
     * @param timeoutUnit
     */
    public MemoryStatsMetricsImporter(String filePath, long timeout, TimeUnit timeoutUnit) {
        super(timeout, timeoutUnit);
        this.filePath = filePath;
        memstats = new Memstats();
        disable = !new File(filePath).exists();
        if (!disable)
            loadIfNessesary();
    }

    @Override
    protected void doRegister(Registry registry) {
        Id id = registry.createId("os.memory.stats");
        MixinMetric mixin = registry.mixinMetric(id);

        mixin.gauge("total.bytes", new Gauge<Long>() {
            @Override
            public Long value() {
                loadIfNessesary();
                return memstats.memTotal;
            }
        });

        mixin.gauge("free.bytes", new Gauge<Long>() {
            @Override
            public Long value() {
                loadIfNessesary();
                return memstats.memFree;
            }
        });
        mixin.gauge("buffers.bytes", new Gauge<Long>() {
            @Override
            public Long value() {
                loadIfNessesary();
                return memstats.buffers;
            }
        });
        mixin.gauge("cached.bytes", new Gauge<Long>() {
            @Override
            public Long value() {
                loadIfNessesary();
                return memstats.cached;
            }
        });

    }

    @Override
    protected void loadValues() {
        try {
            List<String> lines = FileUtils.readFileAsStringArray(filePath);
            Memstats ms = new Memstats();
            int i = 0;
            for (String line : lines) {
                if (i == 4)
                    break;
                String[] infos = line.split(SPLIT);
                if (infos == null) {
                    continue;
                }
                if ("MemTotal:".equals(infos[0])) {
                    ms.memTotal = Long.parseLong(infos[1]) * 1024;
                    i++;
                    continue;
                }
                if ("MemFree:".equals(infos[0])) {
                    ms.memFree = Long.parseLong(infos[1]) * 1024;
                    i++;
                    continue;
                }
                if ("Buffers:".equals(infos[0])) {
                    ms.buffers = Long.parseLong(infos[1]) * 1024;
                    i++;
                    continue;
                }
                if ("Cached:".equals(infos[0])) {
                    ms.cached = Long.parseLong(infos[1]) * 1024;
                    i++;
                    continue;
                }
            }
            memstats = ms;
        } catch (Exception e) {
            logger.debug("warning,can't parse line at /proc/meminfo", e.getMessage());
        }
    }

    private class Memstats {
        long memTotal;
        long memFree;
        long cached;
        long buffers;
    }
}
