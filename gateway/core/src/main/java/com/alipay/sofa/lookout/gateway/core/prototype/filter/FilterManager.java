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

import java.util.Map;
import java.util.function.Predicate;

/**
 * filterManager 其实也是一个filter, 复合了多个filter, 但目前不让它实现filter接口.
 *
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
public interface FilterManager<T> {
    Filter.FilterResult SUCCESS = Filter.SUCCESS;

    Filter.FilterResult test(T t, Map<String, ?> filterContext);

    default Predicate<T> asPredicate(Map<String, ?> filterContext) {
        return t -> test(t, filterContext) == SUCCESS;
    }
}
