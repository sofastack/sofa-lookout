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
package com.alipay.sofa.lookout.gateway.metrics.importer.opentsdb;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;

import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/26
 */
public final class OpentsdbConverter {
    private OpentsdbConverter() {
    }

    /**
     * 将一个opentsdb上传的model转成我们的model
     *
     * @param json
     * @return
     */
    public static Metric convertToModel(JSONObject json) {

        Metric m = new Metric();
        // opentsdb时间戳单位是秒
        m.setTimestamp(json.getLongValue("timestamp") * 1000L);
        m.setName(json.getString("metric"));
        m.setValue(json.getDoubleValue("value"));
        Map<String, String> mtags = m.getTags();

        JSONObject tags = json.getJSONObject("tags");
        if (tags != null) {
            for (Map.Entry<String, Object> e : tags.entrySet()) {
                String key = MetricUtils.formatMetricTagKey(e.getKey());
                String value = MetricUtils.formatTagValue(e.getValue().toString());
                mtags.put(key, value);
            }
        }

        return m;
    }
}
