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

import com.alipay.lookout.core.config.LookoutConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2017/5/2.
 */
public class RealAgentServerUrlTest {

    @Test
    public void testRealAgentServerUrl() {
        LookoutConfig config = new LookoutConfig();
        HttpObserver httpObserver = new HttpObserver(config, new DefaultAddressService());
        Address address = new Address("aa", 8080);
        String url = httpObserver.buildRealAgentServerURL(address);
        System.out.println(url);
        Assert.assertTrue(url.contains("8080"));
    }

    @Test
    public void testRealAgentServerUrlWithSpecialPort() {
        LookoutConfig config = new LookoutConfig();
        config.setProperty(LookoutConfig.LOOKOUT_AGENT_SERVER_PORT, "777");
        HttpObserver httpObserver = new HttpObserver(config, new DefaultAddressService());
        Address address = new Address("aa", 8080);
        String url = httpObserver.buildRealAgentServerURL(address);
        System.out.println(url);
        Assert.assertFalse(url.contains("8080"));
        Assert.assertTrue(url.contains("777"));

    }
}
