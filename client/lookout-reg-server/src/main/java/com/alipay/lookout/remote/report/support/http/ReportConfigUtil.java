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
package com.alipay.lookout.remote.report.support.http;

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-20 20:19
 **/
class ReportConfigUtil {
    private static final Logger          logger                       = LoggerFactory
                                                                          .getLogger(ReportConfigUtil.class);
    //jvm.memory,rpc.consumer
    public static final String           METRIC_NAME_PREFIX_WHITELIST = "name_pre_wl";
    //k1=v1,k2=v2
    public static final String           TAG_WHITELIST                = "tag_wl";

    private ReportConfig                 reportConfig                 = EMPTY_CONFIG;
    private volatile List<String>        metricNamePrefixWhitelist    = null;
    private volatile Map<String, String> tagWhitelist                 = null;
    private static final ReportConfig    EMPTY_CONFIG                 = new ReportConfig();
    private final ResultConsumer         configResultConsumer         = newResultConsumer();

    private ResultConsumer newResultConsumer() {
        return new ResultConsumer() {
            @Override
            public void consume(HttpEntity entity) {
                try {
                    String conf = EntityUtils.toString(entity, "UTF-8");
                    setReportConfig(StringUtils.isEmpty(conf) ? EMPTY_CONFIG : JSON.parseObject(
                        conf, ReportConfig.class));
                    logger.info("receive a new report config,id:{}", reportConfig.getId());
                } catch (Throwable e) {
                    logger.warn("fail to resolve the  fresh report config response.{}",
                        e.getMessage());
                }
            }
        };
    }

    ResultConsumer getConfigResultConsumer() {
        return configResultConsumer;
    }

    private synchronized void setReportConfig(ReportConfig reportConfig) {
        this.reportConfig = reportConfig;
        buildMetricNamePrefixWhitelist();
        buildTagWhitelist();
    }

    public List<LookoutMeasurement> filter(List<LookoutMeasurement> measures) {
        List<String> mnps = metricNamePrefixWhitelist;
        Map<String, String> tagFilters = tagWhitelist;
        if (mnps == null && tagFilters == null) {
            return measures;
        }
        Iterator<LookoutMeasurement> it = measures.iterator();
        while (it.hasNext()) {
            boolean included = false;
            LookoutMeasurement measurement = it.next();
            if (mnps != null) {
                for (String prefix : mnps) {
                    if (measurement.metricId().name().startsWith(prefix)) {
                        included = true;
                        break;//符合一个条件
                    }
                }
            }
            if (!included && tagFilters != null) {
                for (Map.Entry<String, String> e : tagFilters.entrySet()) {
                    if (measurement.containsTag(e.getKey())
                        && e.getValue().equals(measurement.getTags().get(e.getKey()))) {
                        included = true;
                        break;//符合一个条件
                    }
                }
            }
            if (!included) {
                it.remove();
            }
        }
        return measures;
    }

    public ReportConfig getReportConfig() {
        return reportConfig;
    }

    private void buildMetricNamePrefixWhitelist() {
        if (reportConfig != null && reportConfig.getConfig() != null) {
            String value = reportConfig.getConfig().get(METRIC_NAME_PREFIX_WHITELIST);
            if (value == null) {
                metricNamePrefixWhitelist = null;
                return;
            }
            metricNamePrefixWhitelist = new ArrayList<String>();
            String[] namePrefixs = StringUtils.split(value, ",");
            if (namePrefixs != null) {
                for (String namePrefix : namePrefixs) {
                    metricNamePrefixWhitelist.add(namePrefix);
                }
            }
        }
    }

    private void buildTagWhitelist() {
        if (reportConfig != null && reportConfig.getConfig() != null) {
            String value = reportConfig.getConfig().get(TAG_WHITELIST);
            if (value == null) {
                tagWhitelist = null;
                return;
            }
            tagWhitelist = new HashMap<String, String>();
            String[] kvs = StringUtils.split(value, ",");
            if (kvs != null) {
                for (String kv : kvs) {
                    if (kv != null && kv.indexOf("=") > 0) {
                        String[] kvArray = StringUtils.split(kv, "=");
                        if (StringUtils.isNotEmpty(kvArray[0])
                            && StringUtils.isNotEmpty(kvArray[1]))
                            tagWhitelist.put(kvArray[0].trim(), kvArray[1].trim());
                    }
                }
            }
        }
    }
}
