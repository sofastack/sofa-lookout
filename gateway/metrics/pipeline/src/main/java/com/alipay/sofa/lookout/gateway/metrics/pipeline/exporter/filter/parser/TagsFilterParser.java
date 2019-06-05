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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.exporter.filter.parser;

import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.parser.FilterParser;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.exporter.filter.ExportFilters;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/29
 */
public class TagsFilterParser implements FilterParser<Metric> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagsFilterParser.class);

    @Override
    public Filter<Metric> parse(Map<String, Object> args) {
        String condition = (String) args.get("condition");
        // instance_id = 000001
        String[] ss = condition.split("=");
        if (ss.length != 2) {
            LOGGER.warn("filter配置错误 {}", args);
            return null;
        } else {
            String key = ss[0].trim();
            String value = ss[1].trim();
            return ExportFilters.tags().tag(key, value).build();
        }
    }
}
