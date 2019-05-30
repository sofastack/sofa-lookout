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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus;

import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
public final class PrometheusConverter {
    private PrometheusConverter() {
    }

    /**
     * 将prometheus的一行转成一个metric cpu.user{instance_id="000001", app="foo", } 80.5
     *
     * @param line
     * @return 解析失败则null
     */
    public static Metric convertToModel(String line) {
        line = line.trim();
        int tagStartPos = line.indexOf('{');
        int tagEndPos = line.indexOf('}');
        int blankPos = line.indexOf(' ', tagEndPos);
        if (blankPos <= 0) {
            return null;
        }
        String name = line.substring(0, tagStartPos < 0 ? blankPos : tagStartPos);
        double value = Double.parseDouble(line.substring(blankPos + 1));

        Metric m = new Metric();
        m.setName(name);
        m.setValue(value);
        // 由外部填充
        m.setTimestamp(-1);
        if (tagStartPos > 0) {
            String tagsStr = line.substring(tagStartPos + 1, tagEndPos);
            for (String x : StringUtils.split(tagsStr, ',')) {
                x = x.trim();
                if (x.isEmpty()) {
                    continue;
                }
                int nextEqIndex = x.indexOf('=');
                String tagKey = x.substring(0, nextEqIndex);
                String tagValue = x.substring(nextEqIndex + 1 + 1, x.length() - 1);
                m.getTags().put(tagKey, MetricUtils.formatTagValue(tagValue));
            }
        }
        return m;
    }

    private static String normalString(String s) {
        return s.startsWith("\"") ? s.substring(1, s.length() - 1) : s;
    }
}
