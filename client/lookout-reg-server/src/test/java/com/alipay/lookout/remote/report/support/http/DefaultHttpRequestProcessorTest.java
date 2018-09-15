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
package com.alipay.lookout.remote.report.support.http;

import com.alipay.lookout.remote.report.AddressService;
import com.alipay.lookout.remote.report.DefaultAddressService;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by kevin.luy@alipay.com on 2018/9/15.
 */
public class DefaultHttpRequestProcessorTest {

    @Test
    public void testGetHttpClient() {
        CloseableHttpClient client = DefaultHttpRequestProcessor.getHttpClent();
        Assert.assertSame(client, DefaultHttpRequestProcessor.getHttpClent());
    }

    @Test(expected = IOException.class)
    public void testSendPostFail() throws IOException {
        AddressService addressService = new DefaultAddressService();
        DefaultHttpRequestProcessor p = new DefaultHttpRequestProcessor(addressService);
        HttpPost httpPost = new HttpPost("http://localhost/ok");
        try {
            httpPost.setEntity(new StringEntity("a"));
        } catch (UnsupportedEncodingException e) {
        }
        p.sendPostRequest(httpPost, null);
    }
}
