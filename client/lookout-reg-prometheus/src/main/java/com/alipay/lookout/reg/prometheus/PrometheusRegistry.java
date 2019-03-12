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
package com.alipay.lookout.reg.prometheus;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.reg.prometheus.common.PromWriter;
import com.alipay.lookout.reg.prometheus.exporter.ExporterServer;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.lookout.core.config.LookoutConfig.APP_NAME;
import static com.alipay.lookout.core.config.LookoutConfig.DEFAULT_PROMETHEUS_EXPORTER_SERVER_PORT;
import static com.alipay.lookout.core.config.LookoutConfig.LOOKOUT_PROMETHEUS_EXPORTER_SERVER_PORT;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class PrometheusRegistry extends DefaultRegistry implements Closeable, CommonTagsAccessor {
    private static final Logger       logger     = LookoutLoggerFactory
                                                     .getLogger(PrometheusRegistry.class);
    private final ExporterServer      exporterServer;
    private final PromWriter          promWriter = new PromWriter();
    private final Map<String, String> commonTags = new ConcurrentHashMap<String, String>();

    public PrometheusRegistry(MetricConfig config) {
        this(Clock.SYSTEM, config);
    }

    public PrometheusRegistry(Clock clock, MetricConfig config) {
        super(clock, config);
        int serverPort = config.getInt(LOOKOUT_PROMETHEUS_EXPORTER_SERVER_PORT,
            DEFAULT_PROMETHEUS_EXPORTER_SERVER_PORT);
        String appName = config.getString(APP_NAME);
        if (StringUtils.isNotEmpty(appName)) {
            setCommonTag("app", appName);
        }
        exporterServer = new ExporterServer(serverPort);
        exporterServer.addMetricsQueryHandler(new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                String promText = "";
                try {
                    promText = getMetricsSnapshot(PrometheusRegistry.this.iterator());
                    byte[] respContents = promText.getBytes("UTF-8");
                    httpExchange.sendResponseHeaders(200, respContents.length);
                    httpExchange.getResponseHeaders().add("Content-Type",
                        "text/plain; charset=UTF-8");
                    httpExchange.getResponseBody().write(respContents);
                    httpExchange.close();
                } finally {
                    logger.debug("{} scrapes prometheus metrics:\n{}", httpExchange
                        .getRemoteAddress().getAddress(), promText);
                }
            }
        });
        exporterServer.start();
        logger.info("lookout client exporter is started. server port:{}", serverPort);
    }

    /**
     * @param metricIterator
     * @return
     */
    private String getMetricsSnapshot(Iterator<Metric> metricIterator) {
        StringBuilder sb = new StringBuilder();
        while (metricIterator.hasNext()) {
            Metric metric = metricIterator.next();
            if (metric instanceof Info) {
                continue;
            }
            LookoutMeasurement measurement = LookoutMeasurement.from(metric, this);
            sb.append(promWriter.printFromLookoutMeasurement(measurement));
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        exporterServer.stop();
        logger.info("lookout client exporter is stopped.");

    }

    @Override
    public String getCommonTagValue(String name) {
        return commonTags.get(name);
    }

    @Override
    public void setCommonTag(String name, String value) {
        if (value == null) {
            commonTags.remove(name);
        } else {
            commonTags.put(name, value);
        }
    }

    @Override
    public void removeCommonTag(String name) {
        commonTags.remove(name);
    }

    @Override
    public Map<String, String> commonTags() {
        return commonTags;
    }
}
