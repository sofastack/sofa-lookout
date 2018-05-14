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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wuqin
 * @version $Id: DiskUsageMetricsImporter.java, v 0.1 2017-03-18 下午5:30 wuqin Exp $$
 */
public class DiskUsageMetricsImporter extends CachedMetricsImporter {

    private final Logger              logger            = LookoutLoggerFactory
                                                            .getLogger(DiskUsageMetricsImporter.class);

    private static final int          TOTAL_LENGTH      = 6;
    private static final String       DEFAULT_FILE_PATH = "/proc/mounts";

    private static final String       SPLIT             = "\\s+";

    private static final List<String> IGNORE_FS_TYPES   = Arrays.asList("cgroup", "debugfs",
                                                            "devtmpfs", "nfs", "rpc_pipefs",
                                                            "rootfs");

    private String                    filePath;
    private Map<String, DiskUsage>    diskUsageByDevice;

    /**
     * default constructor
     */
    public DiskUsageMetricsImporter() {
        this(DEFAULT_FILE_PATH, DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param filePath
     * @param timeout
     * @param timeoutUnit
     */
    public DiskUsageMetricsImporter(String filePath, long timeout, TimeUnit timeoutUnit) {
        super(timeout, timeoutUnit);
        this.filePath = filePath;
        diskUsageByDevice = new HashMap<String, DiskUsage>();
        loadIfNessesary();
    }

    @Override
    public void register(Registry registry) {
        for (Map.Entry<String, DiskUsage> entry : diskUsageByDevice.entrySet()) {
            final String device = entry.getKey();
            Id id = registry.createId("os.disk.usage." + device);
            id = id.withTag("device", device);
            id = id.withTag("root", diskUsageByDevice.get(device).fsFile);
            id = id.withTag("type", diskUsageByDevice.get(device).fsVfType);
            MixinMetric mixin = registry.mixinMetric(id);

            mixin.gauge("total.bytes", new Gauge<Long>() {
                @Override
                public Long value() {
                    loadIfNessesary();
                    return diskUsageByDevice.get(device).totalBytes;
                }
            });

            mixin.gauge("used.bytes", new Gauge<Long>() {
                @Override
                public Long value() {
                    loadIfNessesary();
                    return diskUsageByDevice.get(device).usedBytes;
                }
            });

            mixin.gauge("percent.used", new Gauge<Float>() {
                @Override
                public Float value() {
                    loadIfNessesary();
                    if (diskUsageByDevice.get(device).totalBytes == 0) {
                        return 0.0f;
                    } else {
                        float ratio = (float) diskUsageByDevice.get(device).usedBytes
                                      / diskUsageByDevice.get(device).totalBytes;
                        return NumFormatUtils.formatFloat(ratio);
                    }
                }
            });

        }
    }

    @Override
    protected void loadValues() {
        try {
            List<String> lines = FileUtils.readFileAsStringArray(filePath);
            for (String line : lines) {
                String[] infos = line.split(SPLIT);
                if (infos == null || infos.length != TOTAL_LENGTH) {
                    continue;
                }

                if (IGNORE_FS_TYPES.contains(infos[DiskMounts.FS_VF_TYPE.ordinal()])
                    || infos[DiskMounts.FS_VF_TYPE.ordinal()].startsWith("fuse.")) {
                    continue;
                }

                String fsFile = infos[DiskMounts.FS_FILE.ordinal()];
                if (fsFile.startsWith("/dev") || fsFile.startsWith("/sys")
                    || fsFile.startsWith("/proc") || fsFile.startsWith("/lib")
                    || fsFile.startsWith("net:")) {
                    continue;
                }

                DiskUsage diskUsage = new DiskUsage();
                diskUsage.fsSpec = infos[DiskMounts.FS_SPEC.ordinal()];
                diskUsage.fsVfType = infos[DiskMounts.FS_VF_TYPE.ordinal()];
                diskUsage.fsFile = fsFile;
                File file = new File(fsFile);
                diskUsage.totalBytes = file.getTotalSpace();
                diskUsage.usedBytes = diskUsage.totalBytes - file.getFreeSpace();
                diskUsageByDevice.put(diskUsage.fsSpec, diskUsage);
            }
        } catch (Exception e) {
            logger.warn("can't parse line at /proc/mounts", e);
        }
    }

    private enum DiskMounts {
        FS_SPEC, // Mounted block special device or remote filesystem
        FS_FILE, // Mount point
        FS_VF_TYPE, // File system type
        FS_MN_OPS, // Mount options
        FS_FREQ, // Dump(8) utility flags
        FS_PASS_NO // Order in which filesystem checks are done at reboot time
    }

    private class DiskUsage {
        String fsSpec;
        String fsFile;
        String fsVfType;
        long   totalBytes;
        long   usedBytes;
    }
}
