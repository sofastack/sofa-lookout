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

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2017/3/16.
 */
public class DefaultAddressServiceTest {

    @Test
    public void testSetTestUrl() {
        AddressService addressService = new DefaultAddressService();
        Assert.assertFalse(addressService.isAgentServerExisted());

        addressService.setAgentTestUrl("100.12.1.12");
        Assert.assertTrue(addressService.isAgentServerExisted());
        Address address = addressService.getAgentServerHost();
        Assert.assertNotNull(address);

    }

    @Test
    public void testSetAgentVip() {
        AddressService addressService = new DefaultAddressService();
        Assert.assertFalse(addressService.isAgentServerExisted());
        addressService.setAgentServerVip("100.12.1.12");
        Assert.assertTrue(addressService.isAgentServerExisted());
        Address address = addressService.getAgentServerHost();
        Assert.assertNotNull(address);
    }


    @Test
    public void testSetAddressList() {
        AddressService addressService = new DefaultAddressService();
        Assert.assertFalse(addressService.isAgentServerExisted());
        ((DefaultAddressService) addressService).setAddressList(Lists.newArrayList("127.0.0.1", "127.0.0.2"));
        Assert.assertTrue(addressService.isAgentServerExisted());
        Address address = addressService.getAgentServerHost();
        System.out.println(address);
        Assert.assertTrue(address.ip().startsWith("127.0.0"));
    }

    @Test
    public void testSetAddressListFromVip() {
        AddressService addressService = new DefaultAddressService();
        Assert.assertFalse(addressService.isAgentServerExisted());
        addressService.setAgentServerVip("127.0.0.1, 127.0.0.2");
        Assert.assertTrue(addressService.isAgentServerExisted());
        Address address = addressService.getAgentServerHost();
        System.out.println(address);
        Assert.assertTrue(address.ip().startsWith("127.0.0"));
    }

}
