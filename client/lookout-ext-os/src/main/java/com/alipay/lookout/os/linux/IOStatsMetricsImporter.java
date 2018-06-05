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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wuqin
 * @version $Id: IOStatsMetricsImporter.java, v 0.1 2017-03-18 下午6:23 wuqin Exp $$
 */
public class IOStatsMetricsImporter extends CachedMetricsImporter {

    private final Logger          logger                    = LookoutLoggerFactory
                                                                .getLogger(IOStatsMetricsImporter.class);

    private static final int      TOTAL_LENGTH              = 14;

    static final int              READ_REQUEST_INDEX        = 3;
    static final int              READ_MERGED_INDEX         = 4;
    static final int              READ_SECTORS_INDEX        = 5;
    static final int              MSEC_READ_INDEX           = 6;
    static final int              WRITE_REQUEST_INDEX       = 7;
    static final int              WRITE_MERGED_INDEX        = 8;
    static final int              WRITE_SECTORS_INDEX       = 9;
    static final int              MSEC_WRITE_INDEX          = 10;
    static final int              IOS_IN_PROGRESS_INDEX     = 11;
    static final int              MSEC_TOTAL_INDEX          = 12;
    static final int              MSEC_WEIGHTED_TOTAL_INDEX = 13;

    private static final String   DEFAULT_FILE_PATH         = "/proc/diskstats";
    private static final String   DEFAULT_UPTIME_FILE_PATH  = "/proc/uptime";

    private static final String   SPLIT                     = "\\s+";

    private String                filePath;
    private String                upTimeFilePath;
    private Map<String, Float[]>  statsByDevice;
    private Map<String, DiskInfo> lastDiskInfoMap;
    private float                 lastUpTime;

    /**
     * default constructor
     */
    public IOStatsMetricsImporter() {
        this(DEFAULT_FILE_PATH, DEFAULT_UPTIME_FILE_PATH, DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param filePath
     * @param upTimeFilePath
     * @param timeout
     * @param timeoutUnit
     */
    public IOStatsMetricsImporter(String filePath, String upTimeFilePath, long timeout,
                                  TimeUnit timeoutUnit) {
        super(timeout, timeoutUnit);
        this.filePath = filePath;
        this.upTimeFilePath = upTimeFilePath;
        this.statsByDevice = new HashMap<String, Float[]>();
        this.lastDiskInfoMap = new HashMap<String, DiskInfo>();
        loadIfNessesary();
    }

    @Override
    public void register(Registry registry) {
        for (Map.Entry<String, Float[]> entry : statsByDevice.entrySet()) {
            final String device = entry.getKey();
            Id id = registry.createId("os.io.stats." + device);
            id = id.withTag("device", device);
            MixinMetric mixin = registry.mixinMetric(id);

            mixin.gauge("svctm", new Gauge<Float>() {
                @Override
                public Float value() {
                    loadIfNessesary();
                    return statsByDevice.get(device)[IOStats.SVCTM.ordinal()];
                }
            });

            mixin.gauge("r_await", new Gauge<Float>() {
                @Override
                public Float value() {
                    loadIfNessesary();
                    return statsByDevice.get(device)[IOStats.R_AWAIT.ordinal()];
                }
            });

            mixin.gauge("w_await", new Gauge<Float>() {
                @Override
                public Float value() {
                    loadIfNessesary();
                    return statsByDevice.get(device)[IOStats.W_AWAIT.ordinal()];
                }
            });

            mixin.gauge("await", new Gauge<Float>() {
                @Override
                public Float value() {
                    loadIfNessesary();
                    return statsByDevice.get(device)[IOStats.AWAIT.ordinal()];
                }
            });

            mixin.gauge("util", new Gauge<Float>() {
                @Override
                public Float value() {
                    loadIfNessesary();
                    return statsByDevice.get(device)[IOStats.UTIL.ordinal()];
                }
            });
        }
    }

    @Override
    protected void loadValues() {
        float deltaTime = collectUpTime();
        if (deltaTime == 0.0f) {
            logger.info("warning,calculate delta time failed!");
            return;
        }

        Map<String, DiskInfo> diskInfoMap = collectDiskInfo();
        for (Map.Entry<String, DiskInfo> entry : diskInfoMap.entrySet()) {
            String device = entry.getKey();
            if (statsByDevice.get(device) == null) {
                statsByDevice.put(device, new Float[IOStats.values().length]);
            }

            calDiskUsage(deltaTime, device, entry.getValue(), statsByDevice.get(device));
        }

        lastDiskInfoMap = diskInfoMap;
    }

    /**
     *
     * @return
     */
    private float collectUpTime() {
        try {
            String line = FileUtils.readFile(upTimeFilePath);
            float currentUpTime = Float.parseFloat(line.split(SPLIT)[0]);
            float deltaTime = currentUpTime - lastUpTime;
            lastUpTime = currentUpTime;
            return deltaTime;
        } catch (Exception e) {
            logger.info("warning,can't parse line at /proc/uptime", e.getMessage());
            return 0.0f;
        }
    }

    /**
     *
     * @return
     */
    private Map<String, DiskInfo> collectDiskInfo() {
        Map<String, DiskInfo> results = new HashMap<String, DiskInfo>();
        try {
            List<String> lines = FileUtils.readFileAsStringArray(filePath);
            for (String line : lines) {
                String[] stats = line.trim().split(SPLIT);
                if (stats == null || stats.length != TOTAL_LENGTH) {
                    logger.info("warning,can't parse text at /proc/diskstats, line: " + line);
                    continue;
                }
                if (Long.parseLong(stats[DiskStats.STATS.ordinal()]) == 0) {
                    continue;
                }

                if (isDevice(stats)) {
                    String device = stats[DiskStats.DEVICE.ordinal()];
                    DiskInfo diskInfo = new DiskInfo();
                    for (int index = DiskStats.STATS.ordinal(); index < stats.length; index++) {
                        long stat = Long.parseLong(stats[index]);
                        switch (index) {
                            case READ_REQUEST_INDEX:
                                diskInfo.readRequests = stat;
                                break;
                            case READ_MERGED_INDEX:
                                diskInfo.readMerged = stat;
                                break;
                            case READ_SECTORS_INDEX:
                                diskInfo.readSectors = stat;
                                break;
                            case MSEC_READ_INDEX:
                                diskInfo.msecRead = stat;
                                break;
                            case WRITE_REQUEST_INDEX:
                                diskInfo.writRequests = stat;
                                break;
                            case WRITE_MERGED_INDEX:
                                diskInfo.writeMerged = stat;
                                break;
                            case WRITE_SECTORS_INDEX:
                                diskInfo.writeSectors = stat;
                                break;
                            case MSEC_WRITE_INDEX:
                                diskInfo.msecWrite = stat;
                                break;
                            case IOS_IN_PROGRESS_INDEX:
                                diskInfo.iosInProgress = stat;
                                break;
                            case MSEC_TOTAL_INDEX:
                                diskInfo.msecTotal = stat;
                                break;
                            case MSEC_WEIGHTED_TOTAL_INDEX:
                                diskInfo.msecWeightedTotal = stat;
                                break;
                        }
                    }

                    results.put(device, diskInfo);
                }
            }
        } catch (Exception e) {
            logger.info("warning,can't parse text at /proc/diskstats", e.getMessage());
        }

        return results;
    }

    private boolean isDevice(String[] stats) {
        if (Long.parseLong(stats[DiskStats.MIN.ordinal()]) % 16 == 0
            && Long.parseLong(stats[DiskStats.MAJ.ordinal()]) > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void calDiskUsage(float deltaTime, String device, DiskInfo diskInfo, Float[] stats) {
        DiskInfo lastDiskInfo = lastDiskInfoMap.get(device);
        if (lastDiskInfo == null) {
            stats[IOStats.UTIL.ordinal()] = 0.0f;
            stats[IOStats.SVCTM.ordinal()] = 0.0f;
            stats[IOStats.R_AWAIT.ordinal()] = 0.0f;
            stats[IOStats.W_AWAIT.ordinal()] = 0.0f;
            stats[IOStats.AWAIT.ordinal()] = 0.0f;
            return;
        }

        long totalRequests = diskInfo.readRequests + diskInfo.writRequests;
        long lastTotalRequests = lastDiskInfo.readRequests + lastDiskInfo.writRequests;
        long totalTicks = diskInfo.msecRead + diskInfo.msecWrite;
        long lastTotalTicks = lastDiskInfo.msecRead + lastDiskInfo.msecWrite;
        float util = 100.0f * (diskInfo.msecTotal - lastDiskInfo.msecTotal) / deltaTime;
        float tPut = NumFormatUtils.formatFloat(100.0f * (totalRequests - lastTotalRequests)
                                                / deltaTime);
        if (tPut > 0) {
            stats[IOStats.SVCTM.ordinal()] = NumFormatUtils.formatFloat(util / tPut);
        } else {
            stats[IOStats.SVCTM.ordinal()] = 0.0f;
        }

        if (diskInfo.readRequests != lastDiskInfo.readRequests) {
            stats[IOStats.R_AWAIT.ordinal()] = NumFormatUtils
                .formatFloat((diskInfo.msecRead - lastDiskInfo.msecRead)
                             / (float) (diskInfo.readRequests - lastDiskInfo.readRequests));
        } else {
            stats[IOStats.R_AWAIT.ordinal()] = 0.0f;
        }

        if (diskInfo.writRequests != lastDiskInfo.writRequests) {
            stats[IOStats.W_AWAIT.ordinal()] = NumFormatUtils
                .formatFloat((diskInfo.msecWrite - lastDiskInfo.msecWrite)
                             / (float) (diskInfo.writRequests - lastDiskInfo.writRequests));
        }
        if (totalRequests != lastTotalRequests) {
            stats[IOStats.AWAIT.ordinal()] = NumFormatUtils
                .formatFloat((totalTicks - lastTotalTicks)
                             / (float) (totalRequests - lastTotalRequests));
        }
        stats[IOStats.UTIL.ordinal()] = NumFormatUtils.formatFloat(util / 1000);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUpTimeFilePath(String upTimeFilePath) {
        this.upTimeFilePath = upTimeFilePath;
    }

    private enum IOStats {
        SVCTM, //平均每次设备I/O操作的服务时间 (毫秒)
        R_AWAIT, //平均每次设备读I/O操作的等待时间(毫秒)
        W_AWAIT, //平均每次设备写I/O操作的等待时间(毫秒)
        AWAIT, //平均每次设备I/O操作的等待时间(毫秒)
        UTIL // 一秒中有百分之多少的时间用于I/O操作
    }

    private enum DiskStats {
        MAJ, MIN, DEVICE, STATS
    }

    private class DiskInfo {
        /**
         * Total number of reads completed successfully.
         */
        private long readRequests;

        /**
         * Adjacent read requests merged in a single req.
         */
        private long readMerged;

        /**
         * Total number of sectors read successfully.
         */
        private long readSectors;

        /**
         * Total number of ms spent by all reads.
         */
        private long msecRead;

        /**
         * Total number of writes completed successfully.
         */
        private long writRequests;

        /**
         * Adjacent write requests merged in a single req.
         */
        private long writeMerged;

        /**
         * Total number of sectors written successfully.
         */
        private long writeSectors;

        /**
         * Total number of ms spent by all writes.
         */
        private long msecWrite;

        /**
         * Number of actual I/O requests currently in flight.
         */
        private long iosInProgress;

        /**
         * Amount of time during which ios_in_progress >= 1.
         */
        private long msecTotal;

        /**
         * Measure of recent I/O completion time and backlog.
         */
        private long msecWeightedTotal;
    }
}
