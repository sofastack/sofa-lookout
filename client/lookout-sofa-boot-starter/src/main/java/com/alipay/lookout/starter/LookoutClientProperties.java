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
package com.alipay.lookout.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.alipay.lookout.core.config.LookoutConfig.DEFAULT_PROMETHEUS_EXPORTER_SERVER_PORT;
import static com.alipay.lookout.core.config.LookoutConfig.DEFAULT_REPORT_BATCH_SIZE;
import static com.alipay.lookout.core.config.MetricConfig.DEFAULT_MAX_METRICS_NUM;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
@ConfigurationProperties(prefix = "com.alipay.sofa.lookout")
public class LookoutClientProperties {
    private boolean enable                       = true;

    private String  agentHostAddress;

    private int     agentServerPort              = -1;

    private long    pollingInterval              = -1l;                                    //mills

    private int     maxMetricsNum                = DEFAULT_MAX_METRICS_NUM;

    private int     reportBatchSize              = DEFAULT_REPORT_BATCH_SIZE;
    private boolean autopollEnable               = true;

    private boolean autopollInfoIgnore           = true;
    private int     prometheusExporterServerPort = DEFAULT_PROMETHEUS_EXPORTER_SERVER_PORT;

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public boolean isEnable() {
        return enable;
    }

    /**
     * lookout.enable
     *
     * @param enable
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getAgentHostAddress() {
        return agentHostAddress;
    }

    public int getAgentServerPort() {
        return agentServerPort;
    }

    public void setAgentServerPort(int agentServerPort) {
        this.agentServerPort = agentServerPort;
    }

    /**
     * lookout.agent-host-address
     *
     * @param agentHostAddress
     */
    public void setAgentHostAddress(String agentHostAddress) {
        this.agentHostAddress = agentHostAddress;
    }

    public int getMaxMetricsNum() {
        return maxMetricsNum;
    }

    public void setMaxMetricsNum(int maxMetricsNum) {
        this.maxMetricsNum = maxMetricsNum;
    }

    public int getReportBatchSize() {
        return reportBatchSize;
    }

    public void setReportBatchSize(int reportBatchSize) {
        this.reportBatchSize = reportBatchSize;
    }

    public boolean isAutopollEnable() {
        return autopollEnable;
    }

    public void setAutopollEnable(boolean autopollEnable) {
        this.autopollEnable = autopollEnable;
    }

    public boolean isAutopollInfoIgnore() {
        return autopollInfoIgnore;
    }

    public void setAutopollInfoIgnore(boolean autopollInfoIgnore) {
        this.autopollInfoIgnore = autopollInfoIgnore;
    }

    public int getPrometheusExporterServerPort() {
        return prometheusExporterServerPort;
    }

    public void setPrometheusExporterServerPort(int prometheusExporterServerPort) {
        this.prometheusExporterServerPort = prometheusExporterServerPort;
    }
}
