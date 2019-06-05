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
package com.alipay.sofa.lookout.gateway.core.common;

import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.List;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/26
 */
public final class WebfluxUtils {
    private WebfluxUtils() {
    }

    public static String getHeaderValue(ServerRequest request, String headerName) {
        return getHeaderValue(request.headers(), headerName);
    }

    public static String getHeaderValue(ServerRequest.Headers headers, String headerName) {
        List<String> valueList = headers.header(headerName);
        return valueList == null || valueList.isEmpty() ? null : valueList.get(0);
    }

    //    @Deprecated
    //    public static String getRemoteIp(ServerRequest serverRequest) {
    //        throw new UnsupportedOperationException();
    //    }
}
