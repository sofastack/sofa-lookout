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

import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.report.AddressService;
import com.alipay.lookout.remote.report.DefaultAddressService;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2018/9/15.
 */
public class ReportDeciderTest {
    AddressService addressService = new DefaultAddressService();
    ReportDecider  reportDecider  = new ReportDecider2(addressService);

    @Test
    public void testChangeSilentTime() {
        Assert.assertFalse(reportDecider.stillSilent());
        reportDecider.changeSilentTime(1, TimeUnit.MINUTES);
        Assert.assertTrue(reportDecider.stillSilent());
    }

    @Test
    public void testRefreshAddressCache() {
        addressService.setAgentServerVip("10.0.0.1");
        reportDecider.refreshAddressCache();
        Assert.assertEquals("10.0.0.1", reportDecider.getAvailableAddress().ip());
    }

    class ReportDecider2 extends ReportDecider {

        public ReportDecider2(AddressService addressService) {
            super(addressService, new LookoutConfig());
        }

        @Override
        public boolean sendPostRequest(HttpPost httpPost, Map<String, String> metadata)
                                                                                       throws IOException {
            return false;
        }

        @Override
        public boolean sendGetRequest(HttpGet httpGet, Map<String, String> metadata)
                                                                                    throws IOException {
            return true;
        }

        @Override
        public boolean sendGetRequest(HttpGet httpGet, Map<String, String> metadata,
                                      ResultConsumer resultConsumer) throws IOException {
            return false;
        }
    }
}
