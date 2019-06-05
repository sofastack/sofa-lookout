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

import com.alipay.sofa.lookout.gateway.core.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
public abstract class AbstractFilterManager<T> implements FilterManager<T> {
    protected final List<Filter<T>>    staticFilters;

    protected volatile List<Filter<T>> filters;

    public AbstractFilterManager(List<Filter<T>> staticFilters) {
        this(staticFilters, null);
    }

    public AbstractFilterManager(List<Filter<T>> staticFilters, List<Filter<T>> dynamicFilters) {
        this.staticFilters = ListUtils.unmodifiableList(staticFilters);
        dynamicFilters = ListUtils.unmodifiableList(dynamicFilters);
        this.filters = mergeFilters(staticFilters, dynamicFilters);
    }

    /**
     * TODO 子类应该重写这个方法对filters进行排序 如果顺序要紧的话
     *
     * @param staticFilters
     * @param dynamicFilters
     * @return
     */
    protected List<Filter<T>> mergeFilters(List<Filter<T>> staticFilters, List<Filter<T>> dynamicFilters) {
        List<Filter<T>> list = new ArrayList<>();
        list.addAll(staticFilters);
        list.addAll(dynamicFilters);
        return list;
    }

    protected void setDynamicFilters(List<Filter<T>> dynamicFilters) {
        this.filters = mergeFilters(staticFilters, ListUtils.unmodifiableList(dynamicFilters));
    }

    protected List<Filter<T>> getFilters() {
        return filters;
    }

    @Override
    public Filter.FilterResult test(T t, Map<String, ?> filterContext) {
        for (Filter<T> filter : filters) {
            Filter.FilterResult result = filter.test(t, filterContext);
            if (result != SUCCESS) {
                return result;
            }
        }
        return SUCCESS;
    }
}
