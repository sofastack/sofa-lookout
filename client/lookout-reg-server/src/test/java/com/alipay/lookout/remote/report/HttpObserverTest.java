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
package com.alipay.lookout.remote.report;

import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.support.http.HttpRequestProcessor;
import com.alipay.lookout.remote.report.support.http.ResultConsumer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/3/15.
 */
public class HttpObserverTest {
    static HttpPost httpPost;

    @Test
    public void testReportSnappy() {
        LookoutConfig config = new LookoutConfig();
        AddressService addressService = new DefaultAddressService();
        Registry registry = new DefaultRegistry();

        HttpObserver observer = new HttpObserver(config, addressService, registry,
            new TestHttpRequestProcessor());
        observer.reportSnappy2Agent(new Address("localhost"), "hellohello", null);
        Assert.assertEquals(12, httpPost.getEntity().getContentLength());
    }

    @Test
    public void testGetBatches() {
        LookoutConfig config = new LookoutConfig();
        AddressService addressService = new DefaultAddressService();
        Registry registry = new DefaultRegistry();

        HttpObserver observer = new HttpObserver(config, addressService, registry);
        List<LookoutMeasurement> measurements = new ArrayList<LookoutMeasurement>();
        measurements.add(new LookoutMeasurement(new Date(), registry.createId("a")));
        measurements.add(new LookoutMeasurement(new Date(), registry.createId("b")));

        List list = observer.getBatches(measurements, 1);
        Assert.assertEquals(2, list.size());
    }

    class TestHttpRequestProcessor implements HttpRequestProcessor {

        @Override
        public Address getAvailableAddress() {
            return null;
        }

        @Override
        public boolean sendPostRequest(HttpPost post, Map<String, String> metadata)
                                                                                   throws IOException {
            httpPost = post;
            return false;
        }

        @Override
        public boolean sendGetRequest(HttpGet httpGet, Map<String, String> metadata)
                                                                                    throws IOException {
            return false;
        }

        @Override
        public boolean sendGetRequest(HttpGet httpGet, Map<String, String> metadata,
                                      ResultConsumer resultConsumer) throws IOException {
            return false;
        }
    }

}
