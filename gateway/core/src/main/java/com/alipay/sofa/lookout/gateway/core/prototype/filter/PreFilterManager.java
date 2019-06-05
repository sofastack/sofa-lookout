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

/**
 * TODO 如果前置过滤器和后置计算过滤器没太大区别的话就做成一个类吧 TODO 集成 动态 能力
 *
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
public class PreFilterManager<T> extends AbstractFilterManager<T> {

    public PreFilterManager(List<Filter<T>> staticFilters) {
        super(staticFilters, null);
    }
}
