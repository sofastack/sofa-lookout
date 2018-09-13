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

import java.util.Random;

/**
 * 动态地址服务
 * Created by kevin.luy@alipay.com on 2017/3/8.
 */
public interface AddressService {
    ThreadLocal<Random> randomThreadLocal = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };

    /**
     * set a agentTestUrl,优先与动态发现的地址
     *
     * @param agentTestUrl agentTestUrl
     */
    void setAgentTestUrl(String agentTestUrl);

    /**
     * set a vip,作为动态发现的地址的兜底方案
     *
     * @param agentServerVip agentServerVip
     */
    void setAgentServerVip(String agentServerVip);

    boolean isAgentServerExisted();

    /**
     * select one address from addresses
     *
     * @return agent address
     */
    Address getAgentServerHost();

    void clearAddressCache();

}
