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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.common;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Tag;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.report.MetricObserver;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 汇报给自己的observer实现
 * Created by kevin.luy@alipay.com on
 * 2017/4/18.
 */
@SuppressWarnings("unchecked")
public class SelfReportObserver implements MetricObserver<LookoutMeasurement> {
    private static final Field METAS_FIELD;

    static {
        try {
            METAS_FIELD = LookoutMeasurement.class.getDeclaredField("metas");
            if (!METAS_FIELD.isAccessible()) {
                METAS_FIELD.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private volatile Consumer<Metric> consumer = x -> {
    };

    private static Map<String, Object> getMetas(LookoutMeasurement m) {
        try {
            return (Map<String, Object>) METAS_FIELD.get(m);
        } catch (IllegalAccessException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
        Consumer<Metric> customer = this.consumer;
        for (LookoutMeasurement m : measures) {
            Id prefixId = m.metricId();
            Map<String, String> prefixTags = new HashMap<>();
            for (Tag tag : prefixId.tags()) {
                prefixTags.put(tag.key(), tag.value());
            }

            Map<String, Object> metas = getMetas(m);

            long timestamp = ((Date) metas.get("time")).getTime();
            Map<String, String> tags = (Map<String, String>) metas.get("tags");

            prefixTags.remove("priority");
            tags.remove("_type_");
            tags.remove("priority");

            for (Map.Entry<String, Object> e : m.getValues().entrySet()) {
                String key = prefixId.name() + "." + e.getKey();
                Metric m2 = new Metric();
                m2.setName(key);
                m2.getTags().putAll(tags);
                m2.getTags().putAll(prefixTags);
                m2.setTimestamp(timestamp);

                Object value = e.getValue();
                if (value instanceof Number) {
                    m2.setValue(((Number) value).doubleValue());
                } else {
                    m2.setInfo(value.toString());
                }
                customer.accept(m2);
            }
        }
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    /**
     * 替换底层consumer
     *
     * @param consumer
     */
    public void setConsumer(Consumer<Metric> consumer) {
        this.consumer = consumer;
    }
}
