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
package com.alipay.sofa.lookout.gateway.metrics.importer.metricbeat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.lookout.gateway.core.common.TimeUtil;
import com.alipay.sofa.lookout.gateway.core.prototype.reader.AbstractReader;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;

import java.util.*;
import java.util.stream.Stream;

/**
 * 解析 metricbeat
 *
 * @author: kevin.luy@antfin.com
 * @date 2018/11/16
 */
public class MetricbeatMetricReader extends AbstractReader<RawMetric, Metric> {
    private static final List<String> TAGS_KEYS = Arrays.asList("metricset", "beat");

    @Override
    public Stream<Metric> read(RawMetric rm) {
        // 按 \n 分隔?
        String str = readUTF8(rm.getRawBody());
        return Arrays.stream(str.split("\n"))
                .flatMap(this::readline)
                .filter(Objects::nonNull);
    }

    /**
     * { "@timestamp": "2018-03-29T08:27:21.200Z", "metricset": { "name": "network", "module": "system", "rtt": 3487 }, "system": {
     * "network": { "in": { "errors": 0, "dropped": 0, "bytes": 0, "packets": 0 }, "out": { "errors": 0, "dropped": 0, "packets": 0,
     * "bytes": 0 }, "name": "ip_vti0" } }, "beat": { "name": "moby", "hostname": "moby", "version": "6.2.3" } }
     *
     * @param inputSource
     * @return
     */
    private Stream<Metric> readline(String inputSource) {
        JSONObject json = JSON.parseObject(inputSource);
        String timestampStr = (String) json.remove("@timestamp");
        long timestamp = TimeUtil.str2Time(timestampStr);

        Map<String, String> tags = new HashMap<>();

        // metricbeat 的tags 放在 metricset beat 里
        for (String tagsKey : TAGS_KEYS) {
            Map<String, Object> itags = (Map<String, Object>) json.remove(tagsKey);
            if (itags != null) {
                itags.remove("rtt");
                for (Map.Entry<String, Object> e : itags.entrySet()) {
                    tags.put(e.getKey(), e.getValue().toString());
                }
            }
        }

        List<Metric> metrics = new ArrayList<>();

        json.remove("type");

        for (Map.Entry<String, Object> e : json.entrySet()) {
            Object value = e.getValue();
            if (value instanceof JSONObject) {
                dfs(e.getKey(), metrics, (JSONObject) value);
            }
        }

        for (Metric m : metrics) {
            m.setTimestamp(timestamp);
            m.setTags(new HashMap<>(tags));
        }
        return metrics.stream();
    }

    private void dfs(String prefix, List<Metric> base, JSONObject json) {
        for (Map.Entry<String, Object> e : json.entrySet()) {
            String key = e.getKey();
            String mergedKey = prefix.isEmpty() ? key : (prefix + "." + key);
            Object value = e.getValue();
            if (value instanceof JSONObject) {
                dfs(mergedKey, base, (JSONObject) value);
            } else if (value instanceof Number) {
                Metric m = new Metric();
                m.setName(mergedKey);
                m.setValue(((Number) value).doubleValue());
                base.add(m);
            }
        }
    }
}
