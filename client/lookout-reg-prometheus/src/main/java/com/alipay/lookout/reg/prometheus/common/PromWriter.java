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
package com.alipay.lookout.reg.prometheus.common;

import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Tag;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class PromWriter {
    private static final Pattern nameChars   = Pattern.compile("[^a-zA-Z0-9_:]");
    private static final Pattern tagKeyChars = Pattern.compile("[^a-zA-Z0-9_]");

    public String writeFromLookoutMeasurement(Collection<LookoutMeasurement> measurements) {
        StringBuilder sb = new StringBuilder();
        for (LookoutMeasurement measurement : measurements) {
            sb.append(printFromLookoutMeasurement(measurement));
        }
        return sb.toString();
    }

    public String printFromLookoutMeasurement(LookoutMeasurement measurement) {
        List<String> tagStrList = new ArrayList<String>();
        for (Map.Entry<String, String> entry : measurement.getTags().entrySet()) {
            tagStrList.add(entry.getKey() + "=\"" + formatString(entry.getValue()) + "\"");
        }
        String tagStr = "";
        if (!tagStrList.isEmpty()) {
            tagStr = "{" + Joiner.on(',').join(tagStrList) + "}";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : measurement.getValues().entrySet()) {
            sb.append(measurement.metricId().name()).append("_").append(entry.getKey())
                .append(tagStr).append(" ").append(entry.getValue().toString()).append("\n");
        }
        return sb.toString();
    }

    public String printFromIndicator(Indicator indicator) {
        List<String> tagStrList = new ArrayList<String>();
        for (Tag tag : indicator.id().tags()) {
            tagStrList
                .add(formatMetricTagKey(tag.key()) + "=\"" + formatString(tag.value()) + "\"");
        }
        String tagStr = "";
        if (!tagStrList.isEmpty()) {
            tagStr = "{" + Joiner.on(',').join(tagStrList) + "}";
        }
        StringBuilder sb = new StringBuilder();
        Collection<Measurement> measurements = indicator.measurements();
        for (Measurement measurement : measurements) {
            sb.append(formatMetricName(indicator.id().name() + "_" + measurement.name()))
                .append(tagStr).append(" ").append(measurement.value().toString()).append("\n");
        }
        return sb.toString();

    }

    private String formatString(String str) {
        return StringEscapeUtils.escapeJson(str);
    }

    public String snakecase(String str) {
        if (!StringUtils.isEmpty(str)) {
            return Joiner.on("_").join(Splitter.on('.').splitToList(str));
        }
        return str;
    }

    public String formatMetricName(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        String sanitized = nameChars.matcher(snakecase(name)).replaceAll("_");
        if (!Character.isLetter(sanitized.charAt(0))) {
            sanitized = "m_" + sanitized;
        }
        return sanitized;
    }

    public String formatMetricTagKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        String sanitized = tagKeyChars.matcher(snakecase(key)).replaceAll("_");
        if (!Character.isLetter(sanitized.charAt(0))) {
            sanitized = "m_" + sanitized;
        }
        return sanitized;
    }

}
