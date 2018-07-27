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
package com.alipay.lookout.core.config;

import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.common.Assert;

import org.apache.commons.configuration2.MapConfiguration;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kevin.luy@alipay.com on 2017/2/23.
 */
public final class LookoutConfig extends MapConfiguration implements MetricConfig {

    public static final String                      LOOKOUT_ENABLE                          = "lookout.enable";
    public static final String                      LOOKOUT_AUTOPOLL_ENABLE                 = "lookout.autopoll.enable";
    public static final String                      LOOKOUT_WEB_SERVER_PORT                 = "lookout.web.server.port";
    public static final String                      LOOKOUT_AGENT_HOST_ADDRESS              = "lookout.agent.host.address";
    public static final String                      LOOKOUT_AGENT_TEST_URL                  = "lookout.agent.host.test.url";
    public static final String                      LOOKOUT_MAX_METRICS_NUMBER              = "lookout.max.metrics.number";
    public static final String                      LOOKOUT_REPORT_BATCH_SIZE               = "lookout.report.batch.size";
    public static final String                      LOOKOUT_REPORT_COMPRESSION_THRESHOLD    = "lookout.report.compression.threshhold";
    public static final String                      LOOKOUT_AUTOPOLL_INFO_METRIC_IGNORE     = "lookout.autopoll.info.ignore";
    public static final String                      LOOKOUT_AGENT_SERVER_PORT               = "lookout.agent.server.port";
    public static final String                      LOOKOUT_ANT_EVENT_LOG_ENABLE            = "lookout.ant.event.log.enable";
    public static final String                      LOOKOUT_PROMETHEUS_EXPORTER_SERVER_PORT = "lookout.prometheus.exporter.server.port";
    // default value
    public static final int                         DEFAULT_WEB_SERVER_PORT                 = 8083;
    public static final int                         DEFAULT_PROMETHEUS_EXPORTER_SERVER_PORT = 9494;
    public static final String                      APP_NAME                                = "app.name";
    public static int                               DEFAULT_REPORT_BATCH_SIZE               = 1700;
    public static final String                      ADDRESS_SERVICE_CLASS_NAME              = "lookout.address.service.class.name";

    /**
     * 对于 xFlush exporter, 多久没有请求拉取数据就进入idle状态
     */
    public static final String                      XFLUSH_EXPORTER_IDLE_SECONDS            = "lookout.xflush.exporter.idle.seconds";

    /**
     * priority->Millsecond
     **/
    private final ConcurrentHashMap<PRIORITY, Long> stepMap                                 = new ConcurrentHashMap<PRIORITY, Long>(
                                                                                                3);

    public LookoutConfig() {
        super(new HashMap<String, Object>());
        stepMap.put(PRIORITY.HIGH, 2000L);//2S
        stepMap.put(PRIORITY.NORMAL, 30000L);//3OS
        stepMap.put(PRIORITY.LOW, 60000L);//1MIN
    }

    public long stepMillis(PRIORITY priority) {
        return stepMap.get(priority);
    }

    public LookoutConfig setStepInterval(PRIORITY priority, long millisecond) {
        Assert.checkArg(millisecond >= 1000, "step interval is illegal!");
        switch (priority) {
            case HIGH:
                Assert.checkArg(millisecond <= stepMap.get(PRIORITY.NORMAL),
                    "interval should to be shorter than NORMAL!");
                break;
            case NORMAL:
                Assert.checkArg(millisecond <= stepMap.get(PRIORITY.LOW),
                    "interval should to be shorter than LOW!");
                Assert.checkArg(millisecond >= stepMap.get(PRIORITY.HIGH),
                    "interval should to be longer than HIGH!");
                break;
            default:
                Assert.checkArg(millisecond >= stepMap.get(PRIORITY.NORMAL),
                    "interval should to be longer than NORMAL!");
        }

        stepMap.put(priority, millisecond);
        return this;
    }

}
