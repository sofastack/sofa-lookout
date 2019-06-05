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

import com.alipay.sofa.lookout.gateway.core.common.TimeUtil;
import com.alipay.sofa.lookout.gateway.core.scrape.JobConfigResolver;
import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-04 11:32
 **/
public class PromJobConfigResolver implements JobConfigResolver {

    private boolean support(Map<String, Object> configsMap) {
        return configsMap.containsKey("metric_scrape_configs");
    }

    @Override
    public List<ScrapeConfig> resolve(String configId, long configLastModifiedTime, Map<String, Object> configsMap) {
        Preconditions.checkArgument(support(configsMap) && configsMap.size() == 1, "Invalid scrape configs!");
        List<Map<String, Object>> jobConfigs = (List<Map<String, Object>>) configsMap.get("metric_scrape_configs");
        List<ScrapeConfig> staticScrapeConfigs = new ArrayList<>();
        for (Map jobConfig : jobConfigs) {
            StaticScrapeConfig staticScrapeConfig = new StaticScrapeConfig();
            staticScrapeConfig.setConfigName(configId);
            staticScrapeConfig.setLastModifiedTime(configLastModifiedTime);
            staticScrapeConfig.setJobName((String) jobConfig.get("job_name"));
            Map<String, List<String>> params = (Map<String, List<String>>) jobConfig.get("params");
            staticScrapeConfig.setParams(params);
            List<Map<String, Object>> staticConfigs = (List<Map<String, Object>>) jobConfig.get("static_configs");
            if (staticConfigs != null && staticConfigs.size() > 0) {
                staticScrapeConfig.setMetricsPath((String) jobConfig.get("metrics_path"));
                String scrapeInterval = (String) jobConfig.get("scrape_interval");
                staticScrapeConfig.setScrapeInterval(TimeUtil.parse(scrapeInterval));
                //TODO
                //FIXME
                //scrapeTimeout
                staticScrapeConfig.setSchema((String) jobConfig.get("scheme"));
                for (Map<String, Object> staticConfig : staticConfigs) {
                    StaticScrapeConfig.StaticConfigItem item = new StaticScrapeConfig.StaticConfigItem();
                    staticScrapeConfig.getStaticConfigItemList().add(item);
                    item.setTargets((List<String>) staticConfig.get("targets"));
                    item.setLabels((Map<String, String>) staticConfig.get("labels"));
                }
            } else {
                throw new UnsupportedOperationException("support static configs only!");
            }
            staticScrapeConfigs.add(staticScrapeConfig);
        }
        return staticScrapeConfigs;
    }
}
