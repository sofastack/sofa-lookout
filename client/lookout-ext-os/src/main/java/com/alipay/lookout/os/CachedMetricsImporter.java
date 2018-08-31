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
package com.alipay.lookout.os;

import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.AbstractRegistry;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.spi.MetricsImporter;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alipay.lookout.core.config.LookoutConfig.LOOKOUT_AUTOPOLL_OS_METRIC_IGNORE;

/**
 * @author wuqin
 * @author kevin.luy@alipay.com
 * @version $Id: CachedMetricsImporter.java, v 0.1 2017-03-18 下午4:40 wuqin Exp $$
 */
public abstract class CachedMetricsImporter implements MetricsImporter {
    private final Logger     logger             = LookoutLoggerFactory
                                                    .getLogger(CachedMetricsImporter.class);
    private static int       MAX_RETRY_TIME     = 3;
    protected static long    DEFAULT_TIMEOUT_MS = 5000;

    private final AtomicLong reloadAt;
    private final long       timeoutMS;
    protected boolean        disable            = false;

    /**
     * Creates a new cached metrics importer with the default timeout period.
     */
    public CachedMetricsImporter() {
        this.reloadAt = new AtomicLong(0);
        this.timeoutMS = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Creates a new cached metrics importer with the given timeout period.
     *
     * @param timeout     the timeout
     * @param timeoutUnit the unit of {@code timeout}
     */
    public CachedMetricsImporter(long timeout, TimeUnit timeoutUnit) {
        this.reloadAt = new AtomicLong(0);
        this.timeoutMS = timeoutUnit.toMillis(timeout);
    }

    protected void loadIfNessesary() {
        if (disable) {
            return;
        }
        for (int retryTimes = 0; retryTimes < MAX_RETRY_TIME; retryTimes++) {
            final long time = System.currentTimeMillis();
            final long current = reloadAt.get();
            if (current > time) {
                return;
            }
            if (reloadAt.compareAndSet(current, time + timeoutMS)) {
                loadValues();
                return;
            }
        }
    }

    @Override
    public void register(Registry registry) {
        if (registry instanceof AbstractRegistry) {
            MetricConfig config = ((AbstractRegistry) registry).getConfig();
            boolean disable = config.getBoolean(LOOKOUT_AUTOPOLL_OS_METRIC_IGNORE, false);
            if (disable) {
                return;
            }
        }
        if (disable) {
            return;
        }
        doRegister(registry);
    }

    protected abstract void doRegister(Registry registry);

    /**
     * Loads the value for all metrics in the set.
     */
    protected abstract void loadValues();
}
