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

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wuqin
 * @version $Id: SystemLoadMetricsImporter.java, v 0.1 2017-03-18 下午4:59 wuqin Exp $$
 */
public class SystemLoadMetricsImporter extends CachedMetricsImporter {

    private final Logger         logger            = LookoutLoggerFactory
                                                       .getLogger(SystemLoadMetricsImporter.class);

    private static final String  DEFAULT_FILE_PATH = "/proc/loadavg";

    private static final Pattern loadPattern       = Pattern
                                                       .compile(
                                                           "^([\\d\\.]+)\\s+([\\d\\.]+)\\s+([\\d\\.]+)\\s+[\\d]+/[\\d]+\\s+([\\d]+).*$",
                                                           Pattern.DOTALL);

    private String               filePath;
    private float[]              loadAvg;

    /**
     * default constructor
     */
    public SystemLoadMetricsImporter() {
        this(DEFAULT_FILE_PATH, DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param filePath
     * @param timeout
     * @param timeoutUnit
     */
    public SystemLoadMetricsImporter(String filePath, long timeout, TimeUnit timeoutUnit) {
        super(timeout, timeoutUnit);
        this.filePath = filePath;
        this.loadAvg = new float[LoadAvg.values().length];
    }

    @Override
    public void register(Registry registry) {
        Id id = registry.createId("os.systemload.average");
        MixinMetric mixin = registry.mixinMetric(id);

        mixin.gauge("1min", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return loadAvg[LoadAvg.ONE_MIN.ordinal()];
            }
        });

        mixin.gauge("5min", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return loadAvg[LoadAvg.FIVE_MIN.ordinal()];
            }
        });

        mixin.gauge("15min", new Gauge<Float>() {
            @Override
            public Float value() {
                loadIfNessesary();
                return loadAvg[LoadAvg.FIFTEEN_MIN.ordinal()];
            }
        });
    }

    @Override
    protected void loadValues() {
        try {
            String loadDetails = FileUtils.readFile(filePath);
            Matcher loadMatcher = loadPattern.matcher(loadDetails);

            if (loadMatcher.matches()) {
                loadAvg[LoadAvg.ONE_MIN.ordinal()] = NumFormatUtils.formatFloat(loadMatcher
                    .group(1));
                loadAvg[LoadAvg.FIVE_MIN.ordinal()] = NumFormatUtils.formatFloat(loadMatcher
                    .group(2));
                loadAvg[LoadAvg.FIFTEEN_MIN.ordinal()] = NumFormatUtils.formatFloat(loadMatcher
                    .group(3));
            }
        } catch (Exception e) {
            logger.warn("can't parse text at /proc/loadavg", e);
        }
    }

    private enum LoadAvg {
        ONE_MIN, FIVE_MIN, FIFTEEN_MIN
    }
}
