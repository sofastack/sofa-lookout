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

import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.remote.report.Address;
import com.alipay.lookout.remote.report.AddressService;
import com.google.common.base.Preconditions;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by kevin.luy@alipay.com on 2018/9/14.
 */
public abstract class ReportDecider implements HttpRequestProcessor {
    private static final Logger      logger                  = LookoutLoggerFactory
                                                                 .getLogger(ReportDecider.class);

    //静默期，一般是得到agent特殊提示，从当前到silentTime这段时间不要再尝试汇报了.
    private volatile long            silentTime              = -1;
    private AtomicReference<Address> addressHolder           = new AtomicReference<Address>();
    private volatile long            addressLastModifiedTime = -1;
    private long                     expiredTime             = 65000;                            //65s

    private AddressService           addressService;
    private MetricConfig             metricConfig;

    public ReportDecider(AddressService addressService, MetricConfig metricConfig) {
        Preconditions.checkNotNull(addressService, "An addressService is required!");
        Preconditions.checkNotNull(metricConfig, "A metricConfig is required!");
        this.addressService = addressService;
        this.addressLastModifiedTime = System.currentTimeMillis() - expiredTime;
        this.metricConfig = metricConfig;
    }

    protected MetricConfig getMetricConfig() {
        return metricConfig;
    }

    public boolean stillSilent() {
        return silentTime > 0 && System.currentTimeMillis() < silentTime;
    }

    void changeSilentTime(int wait, TimeUnit timeUnit) {
        if (wait > 0) {
            long waitTime = timeUnit.toMillis(wait) + System.currentTimeMillis();
            if (waitTime > silentTime) {
                //do change
                silentTime = waitTime;
            }
        }
    }

    /**
     * 保证一定时间(2min)内，只使用同一个 gateway 地址连接上报（优化连接使用）
     *
     * @return
     */
    public synchronized Address getAvailableAddress() {
        if (isAddressExpired()) {
            refreshAddressCache();
        }
        return addressHolder.get();
    }

    /**
     * post error or timeout.(容忍临时并发刷新)
     */
    void refreshAddressCache() {
        //get a new one
        Address oldOne = addressHolder.get();
        Address newOne = addressService.getAgentServerHost();
        if (newOne == null) {
            logger.debug("No gateway address found!");
            return;
        }
        //check new address
        try {
            boolean ok = sendGetRequest(
                new HttpGet(String.format("http://%s:%d/datas", newOne.ip(), newOne.port())), null);
            if (!ok) {
                return;
            }
            // address is checked!
            if (oldOne == null) {
                addressHolder.set(newOne);
            } else if (!newOne.ip().equals(oldOne.ip())) {
                addressHolder.compareAndSet(oldOne, newOne);
            }
            addressLastModifiedTime = System.currentTimeMillis();
            logger.debug("change gateway address ,from {} to {} .", oldOne, newOne);

            return;
        } catch (Throwable e) {
            logger.debug("check gateway address {} fail:{}. old address:{}!", newOne.ip(),
                e.getMessage(), oldOne == null ? "" : oldOne.ip());

        }

    }

    private boolean isAddressExpired() {
        return addressLastModifiedTime + expiredTime < System.currentTimeMillis();
    }

}
