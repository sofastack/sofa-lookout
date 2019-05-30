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

/**
 * 通过异常的方式封装一个过滤结果
 *
 * @author xiangfeng.xzc
 * @date 2018/11/26
 */
public class FilterException extends RuntimeException {
    private final Filter.FilterResult result;

    public FilterException(Filter.FilterResult result) {
        this.result = result;
    }

    public FilterException(String message, Filter.FilterResult result) {
        super(message);
        this.result = result;
    }

    public FilterException(String message, Throwable cause, Filter.FilterResult result) {
        super(message, cause);
        this.result = result;
    }

    public FilterException(Throwable cause, Filter.FilterResult result) {
        super(cause);
        this.result = result;
    }

    public FilterException(String message, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace, Filter.FilterResult result) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.result = result;
    }

    public Filter.FilterResult getResult() {
        return result;
    }
}
