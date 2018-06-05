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
package com.alipay.lookout.remote;

import com.alipay.lookout.MockHttpRequestProcessor;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.AddressService;
import com.alipay.lookout.remote.report.DefaultAddressService;
import com.alipay.lookout.remote.report.HttpObserver;
import com.alipay.lookout.remote.report.support.ReportDecider;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/5/28.
 */
public class HttpObserverTest {

    @Test
    public void testIsEnable() {
        LookoutConfig config = new LookoutConfig();
        AddressService addressService = new DefaultAddressService();
        ReportDecider reportDecider = new ReportDecider();
        Registry registry = new DefaultRegistry();

        HttpObserver observer = new HttpObserver(config, addressService, registry, reportDecider);
        Assert.assertFalse(observer.isEnable());
    }

    @Test
    public void testIsEnable2() {
        LookoutConfig config = new LookoutConfig();
        AddressService addressService = new DefaultAddressService();
        addressService.setAgentTestUrl("127.0.0.1");

        ReportDecider reportDecider = new ReportDecider();
        reportDecider.markPassed();
        Registry registry = new DefaultRegistry();

        HttpObserver observer = new HttpObserver(config, addressService, registry, reportDecider);
        Assert.assertTrue(observer.isEnable());
    }

    @Test
    public void testUpdateMeasurements() {
        LookoutConfig config = new LookoutConfig();
        AddressService addressService = new DefaultAddressService();
        addressService.setAgentTestUrl("127.0.0.1");

        ReportDecider reportDecider = new ReportDecider();
        reportDecider.markPassed();
        Registry registry = new DefaultRegistry();
        MockHttpRequestProcessor requestProcessor = new MockHttpRequestProcessor();
        HttpObserver observer = new HttpObserver(config, addressService, registry, reportDecider,
            requestProcessor);

        List<LookoutMeasurement> measures = new ArrayList<LookoutMeasurement>();
        LookoutMeasurement measurement = new LookoutMeasurement(new Date(),
            new DefaultRegistry().createId("a.b.c"));
        measures.add(measurement);

        observer.update(measures, new HashMap<String, String>());
        Assert.assertNotNull(requestProcessor);
    }
}
