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
package com.alipay.sofa.lookout.gateway.metrics.importer.standard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.lookout.gateway.core.common.TimeUtil;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
@SuppressWarnings("unchecked")
public final class StandardMetricConverter {
    private StandardMetricConverter() {
    }

    /**
     * 标准格式包含了多个metrics
     *
     * @param inputSource
     * @return
     */
    public static Stream<Metric> parse(String inputSource) {
        // TODO 感觉标准格式得改一下, 怎么解析起来这么麻烦?

        JSONObject json = JSON.parseObject(inputSource);

        long timestamp = parseTime(json.getString("time"));

        boolean[] isInfo = {false};
        Map<String, String> tags = new HashMap<>();
        json.getJSONObject("tags")
                .forEach((k, v) -> {
                    // 1.4.2 版本支持
                    if ("_type_".equals(k) && v.equals("i")) {
                        isInfo[0] = true;
                        //忽略该tag
                        return;
                    }
                    // ignore
                    if ("_type_".equals(k) || "priority".equals(k)) {
                        return;
                    }
                    tags.put(MetricUtils.formatMetricTagKey(k), MetricUtils.formatTagValue(v.toString()));
                });

        json.remove("time");
        json.remove("tags");

        return json.entrySet()
                .stream()
                .flatMap(entry -> {
                    String keyPrefix = entry.getKey();
                    Object entryValue = entry.getValue();
                    if (!(entryValue instanceof Map)) {
                        if(entryValue instanceof Number){
                            Metric m = new Metric();
                            m.setName(keyPrefix);
                            m.setTimestamp(timestamp);
                            m.getTags().putAll(tags);
                            m.setValue(((Number) entryValue).doubleValue());
                            return Stream.of(m);
                        }
                        return Stream.empty();
                    }
                    Map<String, Object> subMetrics = (Map<String, Object>) entryValue;
                    return subMetrics.entrySet()
                            .stream()
                            .map(subEntry -> {
                                String key = MetricUtils.formatMetricName(keyPrefix + "." + subEntry.getKey());
                                Object value = subEntry.getValue();

                                Metric m = new Metric();
                                m.setName(key);
                                m.setTimestamp(timestamp);
                                m.getTags().putAll(tags);

                                if (isInfo[0]) {
                                    m.setInfo(value.toString());
                                } else {
                                    m.setValue(((Number) value).doubleValue());
                                }
                                return m;
                            });
                });
    }

    private static long parseTime(String time) {
        // standard客户端上报使用的是这个格式 2018-11-29T15:57:00+08:00
        if (time.length() == 25) {
            return TimeUtil.str2Time(time);
        }
        try {
            return Long.parseLong(time);
        } catch (Exception e) {
            return TimeUtil.str2Time(time);
        }
    }

}
