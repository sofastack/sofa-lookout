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
package com.alipay.lookout.remote.model;

import com.alipay.lookout.api.*;
import com.alipay.lookout.common.Assert;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.common.MeasurementUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/2/7.
 */
public class LookoutMeasurement {
    public static final String        BRACES_LEFT  = "{";
    public static final String        BRACES_RIGHT = "}";
    public static final String        COMMA        = ",";
    public static final String        QUOTE        = "\"";
    public static final String        COLON        = ":";
    static final String               TIME_KEY     = "time";
    static final String               TAGS_KEY     = "tags";
    private final Map<String, Object> values       = new LinkedHashMap<String, Object>(2);
    private final Map<String, Object> metas        = new LinkedHashMap<String, Object>(2);
    //reference cache
    private final Map<String, String> tags;

    private final Id                  metricId;

    public LookoutMeasurement(Date date, Id id) {
        metas.put(TIME_KEY, date);
        tags = new HashMap<String, String>();
        metas.put(TAGS_KEY, tags);
        this.metricId = id;
    }

    public Id metricId() {
        return metricId;
    }

    public void addTag(String tagName, String tagValue) {
        tags.put(tagName, tagValue);
    }

    public boolean containsTag(String key) {
        return tags.containsKey(key);
    }

    public void put(String key, Object value) {
        //        Assert.checkArg(!TIME_KEY.equalsIgnoreCase(key),"");
        //        Assert.checkArg(!TAGS_KEY.equalsIgnoreCase(key),"");
        values.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        tojson(stringBuilder, values, metas);
        return stringBuilder.toString();
    }

    private StringBuilder tojson(StringBuilder stringBuilder, Map<String, Object> values,
                                 Map<String, Object> metas) {
        stringBuilder.append(BRACES_LEFT);
        map2json(stringBuilder, metas);
        Map<String, Object> map = new HashMap<String, Object>(1);
        map.put(metricId.name(), values);
        stringBuilder.append(COMMA);
        map2json(stringBuilder, map);
        stringBuilder.append(BRACES_RIGHT);
        return stringBuilder;
    }

    private StringBuilder map2json(StringBuilder stringBuilder, Map<String, Object> map) {
        if (!map.isEmpty()) {
            int size = map.size();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                //key print
                stringBuilder.append(QUOTE).append(entry.getKey()).append(QUOTE).append(COLON);
                //key:tags,tags:value
                if (entry.getValue() instanceof Map) {
                    stringBuilder.append(BRACES_LEFT);
                    map2json(stringBuilder, (Map<String, Object>) entry.getValue());
                    stringBuilder.append(BRACES_RIGHT);
                }
                //key:time,time:value
                else if (entry.getValue() instanceof Date) {
                    String timestamp = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT
                        .format((Date) (entry.getValue()));
                    stringBuilder.append(QUOTE).append(timestamp).append(QUOTE);
                }
                //value:number
                else if (entry.getValue() instanceof Number) {
                    stringBuilder.append(entry.getValue());
                } else {
                    stringBuilder.append(QUOTE)
                        .append(StringEscapeUtils.escapeJson(entry.getValue().toString()))
                        .append(QUOTE);
                }
                if (--size > 0) {
                    stringBuilder.append(COMMA);
                }
            }
        }
        return stringBuilder;
    }

    /**
     * Getter method for property <tt>tags</tt>.
     *
     * @return property value of tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Getter method for property <tt>values</tt>.
     *
     * @return property value of values
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * @param metric             带解析的metric对象
     * @param commonTagsAccessor 方便添加common tags,可以为null
     * @return
     */
    public static LookoutMeasurement from(Metric metric, CommonTagsAccessor commonTagsAccessor) {
        Indicator indicator = metric.measure();
        Id id = metric.id();
        LookoutMeasurement measurement = new LookoutMeasurement(new Date(indicator.getTimestamp()),
            id);
        for (Object mObj : indicator.measurements()) {
            Measurement m = (Measurement) mObj;
            Assert.notNull(m.name(), String.format("empty measure name,metric: %s!", metric.id()));
            //printValue for info metric
            measurement.put(m.name(), MeasurementUtil.printValue(m.value()));
        }
        for (Tag tag : id.tags()) {
            measurement.addTag(tag.key(), tag.value());
        }
        // add common tags,If already assigned then ignore
        if (commonTagsAccessor != null) {
            Map<String, String> commonTags = (commonTagsAccessor).commonTags();
            for (Map.Entry<String, String> tagEntry : commonTags.entrySet()) {
                if (!measurement.containsTag(tagEntry.getKey())) {
                    measurement.addTag(tagEntry.getKey(), tagEntry.getValue());
                }
            }
        }
        return measurement;
    }
}
