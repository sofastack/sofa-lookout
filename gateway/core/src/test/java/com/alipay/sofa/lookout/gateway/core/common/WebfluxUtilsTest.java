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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 10:00
 **/
public class WebfluxUtilsTest {

    @Test
    public void testGetHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("app", "demo");
        ServerRequest req = MockServerRequest.builder().headers(headers).build();
        Assert.assertEquals("demo", WebfluxUtils.getHeaderValue(req, "app"));
    }
}
