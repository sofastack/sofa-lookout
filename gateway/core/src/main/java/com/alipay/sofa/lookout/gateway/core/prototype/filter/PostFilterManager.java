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
package com.alipay.sofa.lookout.gateway.core.prototype.filter;

import java.util.List;
import java.util.Map;

/**
 * TODO 集成 动态 能力
 *
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
public class PostFilterManager<T> extends AbstractFilterManager<T> {
    public PostFilterManager(List<Filter<T>> staticFilters) {
        super(staticFilters);
    }

    @Override
    public Filter.FilterResult test(T m, Map<String, ?> filterContext) {
        for (Filter<T> filter : filters) {
            Filter.FilterResult result = filter.test(m, filterContext);
            if (result != SUCCESS) {
                // TODO CONTEXT.getDebugId ,AND LOG INFO;
                //                if (m.getDebugId() != null) {
                //                    LogUtils.DEBUG_LOGGER.info("debugId={} {} 过滤失败", m.getDebugId(), filter.name());
                //                }
                return result;
            }
        }
        return SUCCESS;
    }
}
