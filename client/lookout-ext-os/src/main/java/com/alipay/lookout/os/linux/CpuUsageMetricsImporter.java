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
import com.alipay.lookout.os.utils.NumFormatUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wuqin
 * @version $Id: CpuUsageMetricsImporter.java, v 0.1 2017-03-18 下午5:19 wuqin Exp $$
 */
public class CpuUsageMetricsImporter extends CachedMetricsImporter {

    private final Logger         logger            = LookoutLoggerFactory
                                                       .getLogger(CpuUsageMetricsImporter.class);

    private static final String  DEFAULT_FILE_PATH = "/proc/stat";

    private static final Pattern cpuStatPattern    = Pattern
                                                       .compile(
                                                           "^.*cpu\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+).*$",
                                                           Pattern.DOTALL);

    private static final int     USER_INDEX        = 1;
    private static final int     NICE_INDEX        = 2;
    private static final int     SYSTEM_INDEX      = 3;
    private static final int     IDLE_INDEX        = 4;
    private static final int     IO_WAIT_INDEX     = 5;
    private static final int     IRQ_INDEX         = 6;
    private static final int     SOFT_IRQ_INDEX    = 7;
    private static final int     STOLEN_INDEX      = 8;
    private static final int     GUEST_INDEX       = 9;

    private String               filePath;
    private float[]              cpuUsage;
    private CpuInfo              lastCpuInfo;

    /**
     * default constructor
     */
    public CpuUsageMetricsImporter() {
        this(DEFAULT_FILE_PATH, DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * @param filePath
     * @param timeout
     * @param timeoutUnit
     */
    public CpuUsageMetricsImporter(String filePath, long timeout, TimeUnit timeoutUnit) {
        super(timeout, timeoutUnit);
        this.filePath = filePath;
        this.cpuUsage = new float[CpuUsage.values().length];
        this.lastCpuInfo = new CpuInfo();
        disable = !new File(filePath).exists();
    }

    @Override
    protected void doRegister(Registry registry) {
        Id id = registry.createId("os.cpu");
        MixinMetric mixin = registry.mixinMetric(id);

        mixin.gauge("user", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.USER.ordinal()];
            }
        });

        mixin.gauge("nice", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.NICE.ordinal()];
            }
        });

        mixin.gauge("system", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.SYSTEM.ordinal()];
            }
        });

        mixin.gauge("idle", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.IDLE.ordinal()];
            }
        });

        mixin.gauge("iowait", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.IOWAIT.ordinal()];
            }
        });

        mixin.gauge("irq", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.IRQ.ordinal()];
            }
        });

        mixin.gauge("softirq", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return cpuUsage[CpuUsage.SOFTIRQ.ordinal()];
            }
        });
    }

    @Override
    protected void loadValues() {
        CpuInfo currentCpuInfo = collectCpuInfo();
        if (currentCpuInfo == null) {
            logger.debug("warning,collect cpu info failed!");
            lastCpuInfo = new CpuInfo();
            return;
        }

        cpuUsage[CpuUsage.USER.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.userTime - lastCpuInfo.userTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));
        cpuUsage[CpuUsage.NICE.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.niceTime - lastCpuInfo.niceTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));
        cpuUsage[CpuUsage.SYSTEM.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.systemTime - lastCpuInfo.systemTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));
        cpuUsage[CpuUsage.IDLE.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.idleTime - lastCpuInfo.idleTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));
        cpuUsage[CpuUsage.IOWAIT.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.ioWaitTime - lastCpuInfo.ioWaitTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));
        cpuUsage[CpuUsage.IRQ.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.irqTime - lastCpuInfo.irqTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));
        cpuUsage[CpuUsage.SOFTIRQ.ordinal()] = NumFormatUtils
            .formatFloat(100.0f * (currentCpuInfo.softIrqTime - lastCpuInfo.softIrqTime)
                         / (currentCpuInfo.totalTime - lastCpuInfo.totalTime));

        lastCpuInfo = currentCpuInfo;
    }

    /**
     * @return
     */
    private CpuInfo collectCpuInfo() {
        try {
            String statResult = FileUtils.readFile(filePath);
            Matcher statMatcher = cpuStatPattern.matcher(statResult);
            if (statMatcher.matches()) {
                CpuInfo cpuInfo = new CpuInfo();
                for (int index = 1; index < statMatcher.groupCount(); index++) {
                    long time = Long.parseLong(statMatcher.group(index));
                    cpuInfo.totalTime += time;
                    switch (index) {
                        case USER_INDEX:
                            cpuInfo.userTime = time;
                            break;
                        case NICE_INDEX:
                            cpuInfo.niceTime = time;
                            break;
                        case SYSTEM_INDEX:
                            cpuInfo.systemTime = time;
                            break;
                        case IDLE_INDEX:
                            cpuInfo.idleTime = time;
                            break;
                        case IO_WAIT_INDEX:
                            cpuInfo.ioWaitTime = time;
                            break;
                        case IRQ_INDEX:
                            cpuInfo.irqTime = time;
                            break;
                        case SOFT_IRQ_INDEX:
                            cpuInfo.softIrqTime = time;
                            break;
                        case STOLEN_INDEX:
                            cpuInfo.stolenTime = time;
                            break;
                        case GUEST_INDEX:
                            cpuInfo.guestTime = time;
                            break;
                    }
                }

                return cpuInfo;
            }
        } catch (Exception e) {
            logger.info("warning,can't parse text at /proc/stat", e.getMessage());
        }

        return null;
    }

    private enum CpuUsage {
        USER, NICE, SYSTEM, IDLE, IOWAIT, IRQ, SOFTIRQ
    }

    private class CpuInfo {
        /**
         * total time
         */
        long totalTime;

        /**
         * The amount of time the system spent in user mode
         */
        long userTime;

        /**
         * The amount of time the system spent in user mode with low priority(nice)
         */
        long niceTime;

        /**
         * The amount of time the system spent in system mode
         */
        long systemTime;

        /**
         * The amount of time the system spent in idle task
         */
        long idleTime;

        /**
         * time waiting for I/O to complete
         */
        long ioWaitTime;

        /**
         * time servicing interrupts
         */
        long irqTime;

        /**
         * time servicing softirqs
         */
        long softIrqTime;

        /**
         * the time spent in other operating systems when running in a virtualized environment
         */
        long stolenTime;

        /**
         * the time spent running a virtual CPU for guest operating systems
         * under the control of the Linux kernel
         */
        long guestTime;
    }
}
