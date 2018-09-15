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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认静态地址服务
 * with vip and test model url
 * Created by kevin.luy@alipay.com on 2017/5/31.
 */
public class DefaultAddressService implements AddressService {
    private Address       agentTestUrl;
    private List<Address> addressList;

    public DefaultAddressService() {
    }

    public DefaultAddressService(String appName) {
    }

    //  cache,for a connection keep alive & reuse;
    public void clearAddressCache() {
    }

    public void setAgentTestUrl(String agentTestUrl) {
        if (!Strings.isNullOrEmpty(agentTestUrl)) {
            this.agentTestUrl = new Address(agentTestUrl);
        }
    }

    public void setAgentServerVip(String agentServerVip) {
        if (Strings.isNullOrEmpty(agentServerVip)) {
            return;
        }
        //multi addresses
        if (agentServerVip.contains(",")) {
            List<String> agentServers = Splitter.on(',').splitToList(agentServerVip);
            setAddressList(agentServers);
            return;
        }
        this.addressList = Lists.newArrayList(new Address(agentServerVip));
    }

    public void setAddressList(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return;
        }
        List<Address> addressList = new ArrayList<Address>();
        for (String addressStr : addresses) {
            addressList.add(new Address(addressStr.trim()));
        }
        this.addressList = addressList;
    }

    protected Address getAgentTestUrl() {
        return agentTestUrl;
    }

    protected List<Address> getAddressList() {
        return addressList;
    }

    @Override
    public boolean isAgentServerExisted() {
        return agentTestUrl != null || (addressList != null && !addressList.isEmpty());
    }

    @Override
    public Address getAgentServerHost() {
        if (agentTestUrl != null) {
            return agentTestUrl;
        }
        List<Address> addrList = addressList;
        if (addrList == null) {
            return null;
        }
        if (addrList.size() == 1) {
            return addrList.get(0);
        }
        int randomNodeIdx = randomThreadLocal.get().nextInt(addrList.size());
        return addrList.get(randomNodeIdx);
    }
}
