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
package com.alipay.sofa.lookout.gateway.core.prototype.filter.parser;

import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;

import java.util.Map;

/**
 * 从配置解析出一个filter
 *
 * @author xiangfeng.xzc
 * @date 2018/11/29
 */
public interface FilterParser<T> {
    /**
     * 因为各个filter需要的参数不一样, 因此只能用一个map来封装参数
     *
     * @param args
     * @return
     */
    Filter<T> parse(Map<String, Object> args);
}
