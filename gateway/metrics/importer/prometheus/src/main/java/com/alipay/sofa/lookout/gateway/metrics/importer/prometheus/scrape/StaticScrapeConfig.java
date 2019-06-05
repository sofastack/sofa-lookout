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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.scrape;


import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-03 20:52
 **/
public class StaticScrapeConfig extends ScrapeConfig {


    //The HTTP resource path on which to fetch metrics from targets
    private String metricsPath = "/metrics";
    private String schema = "http";
    private String basicAuthUserName;
    private String basicAuthPassword;
    private String tlsConfig;
    private List<StaticConfigItem> staticConfigItemList = new ArrayList<>();
    private Map<String,List<String>> params;

    public Map<String, List<String>> getParams() {
        return params;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public String getMetricsPath() {
        return metricsPath;
    }

    public void setMetricsPath(String metricsPath) {
        this.metricsPath = metricsPath;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        //keep default http
        if(schema==null){
            return;
        }
        this.schema = schema;
    }

    public String getBasicAuthUserName() {
        return basicAuthUserName;
    }

    public void setBasicAuthUserName(String basicAuthUserName) {
        this.basicAuthUserName = basicAuthUserName;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }

    public String getTlsConfig() {
        return tlsConfig;
    }

    public void setTlsConfig(String tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    public List<StaticConfigItem> getStaticConfigItemList() {
        return staticConfigItemList;
    }

    public void setStaticConfigItemList(List<StaticConfigItem> staticConfigItemList) {
        this.staticConfigItemList = staticConfigItemList;
    }

    public static class StaticConfigItem {
        private List<String> targets = new ArrayList<>();
        private Map<String, String> labels = new HashMap<>();

        public List<String> getTargets() {
            return targets;
        }

        public void setTargets(List<String> targets) {
            this.targets = targets;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }
    }
}
