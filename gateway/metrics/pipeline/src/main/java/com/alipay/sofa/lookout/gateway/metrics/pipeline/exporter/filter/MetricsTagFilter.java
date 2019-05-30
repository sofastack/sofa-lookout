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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.exporter.filter;

import com.alipay.sofa.lookout.gateway.core.prototype.filter.AbstractFilter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * tags过滤器
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class MetricsTagFilter extends AbstractFilter<Metric> {
    private final Map<String, String> requiredTags;

    public MetricsTagFilter(Map<String, String> requiredTags) {
        super("tags");
        // 复制一份 保证安全
        this.requiredTags = new HashMap<>(Preconditions.checkNotNull(requiredTags));
    }

    @Override
    public String type() {
        return "metric";
    }

    @Override
    public Filter.FilterResult test(Metric metric, Map<String, ?> filterContext) {
        Map<String, String> tags = metric.getTags();
        for (Map.Entry<String, String> e : requiredTags.entrySet()) {
            String v = tags.get(e.getKey());
            if (v == null || !v.equals(e.getValue())) {
                return fail("missing required tag " + e.getKey());
            }
        }
        return SUCCESS;
    }
}
