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

import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.utils.NetworkUtil;
import com.alipay.lookout.remote.report.Address;
import com.alipay.lookout.remote.report.AddressService;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.alipay.lookout.common.LookoutConstants.LOW_PRIORITY_TAG;
import static com.alipay.lookout.remote.report.SchedulerPoller.PRIORITY_NAME;

/**
 * 将数据推送到 lookout-gateway 上
 * Created by kevin.luy@alipay.com on 2017/4/13.
 */
public final class DefaultHttpRequestProcessor implements HttpRequestProcessor {
    private static final Logger        logger                       = LookoutLoggerFactory
                                                                        .getLogger(DefaultHttpRequestProcessor.class);

    public static final String         CLIENT_IP_HEADER_NAME        = "Client-Ip";
    public static final String         LOOKOUT_REPORT_FAIL_COUNT_ID = "lookout.report.fail";
    public static final String         WAIT_MINUTES                 = "Wait-Minutes";
    static final String                CLIENT_VERSION               = "LOOKOUT-CLIENT-V1";
    private final String               clientIp                     = NetworkUtil.getLocalAddress()
                                                                        .getHostAddress();

    final static RequestConfig         reqConf                      = buildRequestConfig();
    //HTTP
    static CloseableHttpClient         httpClientCache;
    private static Runnable            clearIdleConnectionsTask;

    private static final AtomicBoolean httpClientInitialized        = new AtomicBoolean(false);

    //静默期，一般是得到agent特殊提示，从当前到silentTime这段时间不要再尝试汇报了.
    private volatile long              silentTime                   = -1;
    private AtomicReference<Address>   addressHolder                = new AtomicReference<Address>();
    private volatile long              addressLastModifiedTime      = -1;
    private long                       expiredTime                  = 65000;                                          //65s

    private AddressService             addressService;
    private final Map<String, String>  commonMetadata               = new HashMap<String, String>();

    public DefaultHttpRequestProcessor(AddressService addressService) {
        this.addressService = addressService;
        this.addressLastModifiedTime = System.currentTimeMillis() - expiredTime;
    }

    @Override
    public void addCommonHeader(String headerName, String headerValue) {
        commonMetadata.put(headerName, headerValue);
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
     * post error or timeout.(容忍临时并发刷新)
     */
    void refreshAddressCache() {
        //get a new one
        Address oldOne = addressHolder.get();
        Address newOne = addressService.getAgentServerHost();
        if (newOne == null) {
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
            logger.debug("check gateway address {} fail :{}!", newOne.ip(), e.getMessage());
        }

    }

    private boolean isAddressExpired() {
        return addressLastModifiedTime + expiredTime < System.currentTimeMillis();
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

    @Override
    public boolean sendGetRequest(final HttpGet httpGet, Map<String, String> metadata)
                                                                                      throws IOException {
        addCommonHeaders(httpGet, metadata);
        httpGet.setConfig(reqConf);
        return sendRequest(httpGet, new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(HttpResponse response) throws IOException {
                try {
                    if (200 != response.getStatusLine().getStatusCode()) {
                        handleErrorResponse(response, httpGet);
                        return false;
                    } else {//success
                        logger.debug("check lookout gateway ok.{}", httpGet.toString());
                    }
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                return true;
            }
        });
    }

    @Override
    public boolean sendPostRequest(final HttpPost httpPost, Map<String, String> metadata)
                                                                                         throws IOException {
        if (PRIORITY.LOW.name().equalsIgnoreCase(metadata.get(PRIORITY_NAME))
            && clearIdleConnectionsTask != null) {
            clearIdleConnectionsTask.run();
        }
        addCommonHeaders(httpPost, metadata);
        httpPost.setConfig(reqConf);
        try {
            return sendRequest(httpPost, new ResponseHandler<Boolean>() {
                @Override
                public Boolean handleResponse(HttpResponse response) throws IOException {
                    try {
                        if (200 != response.getStatusLine().getStatusCode()) {
                            refreshAddressCache();
                            handleErrorResponse(response, httpPost);
                            Registry r = Lookout.registry();
                            r.counter(
                                r.createId(LOOKOUT_REPORT_FAIL_COUNT_ID).withTag(LOW_PRIORITY_TAG))
                                .inc();
                            return false;
                        } else {//success
                            logger.debug("<< report to lookout gateway ok.{}", httpPost.toString());
                        }
                    } finally {
                        EntityUtils.consumeQuietly(response.getEntity());
                    }
                    return true;
                }
            });
        } catch (IOException e) {
            //try with a new address
            refreshAddressCache();
            throw e;
        }

    }

    private void addCommonHeaders(HttpRequestBase httpPost, Map<String, String> metadata) {
        httpPost.setHeader(CLIENT_IP_HEADER_NAME, clientIp);
        if (metadata == null) {
            metadata = new HashMap<String, String>();
        }
        metadata.putAll(commonMetadata);
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }
    }

    private <T> T sendRequest(HttpRequestBase requestBase, ResponseHandler<T> responseHandler)
                                                                                              throws IOException {
        CloseableHttpClient httpClient = getHttpClent();
        if (httpClient == null) {
            return null;
        }
        return httpClient.execute(requestBase, responseHandler);
    }

    public void handleErrorResponse(HttpResponse response, HttpRequestBase request) {
        int status = response.getStatusLine().getStatusCode();
        Header header = response.getFirstHeader("Err");
        String errMsg = (header != null && header.getValue() != null) ? header.getValue() : "";
        if (401 == status) {
            logger.info(">>WARNING: Unauthorized!msg:{},request:{}", errMsg, request.toString());
        } else if (403 == status) {
            logger.info(">>WARNING: Forbidden!msg:{},request:{}", errMsg, request.toString());
        } else if (555 == status) {
            logger.info(">>WARNING: gateway current limit!msg:{},request:{}", errMsg,
                request.toString());
        } else {
            logger.info(">>WARNING: send to gateway fail!status:{}!msg:{}request:{}", status,
                errMsg, request.toString());
        }
        //change silentTime
        if (response.containsHeader(WAIT_MINUTES)) {
            String waitMinutesStr = response.getFirstHeader(WAIT_MINUTES).getValue();
            changeSilentTime(waitMinutesStr);
        }
    }

    private void changeSilentTime(String waitMinutesStr) {
        int wait = -1;
        try {
            wait = Integer.valueOf(waitMinutesStr);
        } catch (Throwable e) {
            logger.info(">>WARNING: Wait-Minutes header value:{} is illegal!", waitMinutesStr);
        }
        changeSilentTime(wait, TimeUnit.MINUTES);
    }

    static CloseableHttpClient getHttpClent() {
        if (httpClientCache != null) {
            return httpClientCache;
        }
        if (httpClientInitialized.compareAndSet(false, true)) {
            final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setDefaultMaxPerRoute(2);
            connManager.setMaxTotal(4);
            httpClientCache = HttpClientBuilder.create().setConnectionManager(connManager)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(1, false))
                .setUserAgent(CLIENT_VERSION).build();
            clearIdleConnectionsTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        connManager.closeIdleConnections(30, TimeUnit.SECONDS);
                        connManager.closeExpiredConnections();
                    } catch (Throwable e) {
                        logger.warn("fail to close idle connections.{}", e.getMessage());
                    }
                }
            };
            return httpClientCache;
        }
        return null; //发生并发初始化情况;
    }

    static RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(1000).setSocketTimeout(1000).build();
    }

}
