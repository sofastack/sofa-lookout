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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.filter.builtin.post;

import com.alipay.sofa.lookout.gateway.core.prototype.filter.AbstractFilter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;

import java.util.Map;

/**
 * 标准metric过滤器, 只要是个metric就必须满足的条件
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class CommonMetricFilter extends AbstractFilter<Metric> {

    private Filter.FilterResult NAME_TOO_LONG          = fail("name to long");
    private Filter.FilterResult TAGS_COUNT_ERROR       = fail("tags count error");
    private Filter.FilterResult TAGS_KEY_TOO_LONG      = fail("tags key too long");
    private Filter.FilterResult METRIC_IS_TOO_OLD      = fail("metric is too old");
    private static final long   TWO_DAY_INTERVAL_MILLS = 2 * 24 * 3600 * 1000L;

    @Override
    public String type() {
        return "metric";
    }

    @Override
    public Filter.FilterResult test(Metric m, Map<String, ?> filterContext) {
        // 规则1: name长度不超过200
        if (m.getName().length() > 200) {
            return NAME_TOO_LONG;
        }

        // 规则2: tags必须有 且 个数不超过20
        Map<String, String> tags = m.getTags();
        if (tags.size() == 0 || tags.size() > 20) {
            return TAGS_COUNT_ERROR;
        }

        // 规则3: tags的key/value的长度不超过100
        for (Map.Entry<String, String> e : tags.entrySet()) {
            int size = e.getKey().length();
            if (size == 0 || size > 100) {
                return TAGS_KEY_TOO_LONG;
            }
            size = e.getValue().length();
            if (size > 200) {
                e.setValue(e.getValue().substring(0, 200));
            }
        }

        // TODO 大量调用currentTimeMillis会有性能影响吗?
        long now = System.currentTimeMillis();
        long timestamp = m.getTimestamp();

        // 不接收2天以前的数据
        if (timestamp + TWO_DAY_INTERVAL_MILLS < now) {
            return METRIC_IS_TOO_OLD;
        }

        return SUCCESS;
    }
}
