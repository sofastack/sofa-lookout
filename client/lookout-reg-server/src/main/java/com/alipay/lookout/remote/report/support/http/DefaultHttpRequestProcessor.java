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
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.utils.NetworkUtil;
import com.alipay.lookout.remote.report.support.ReportDecider;
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
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.lookout.common.LookoutConstants.LOW_PRIORITY_TAG;

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

    final ReportDecider                reportDecider;

    final static RequestConfig         reqConf                      = buildRequestConfig();
    //HTTP
    static CloseableHttpClient         httpClientCache;
    private static final AtomicBoolean httpClientInitialized        = new AtomicBoolean(false);

    public DefaultHttpRequestProcessor(ReportDecider reportDecider) {
        this.reportDecider = reportDecider;
    }

    @Override
    public void sendPostRequest(HttpPost httpPost, Map<String, String> metadata) throws IOException {
        addCommonHeaders(httpPost, metadata);
        httpPost.setConfig(reqConf);
        sendRequest(httpPost);
    }

    private void addCommonHeaders(HttpRequestBase httpPost, Map<String, String> metadata) {
        httpPost.setHeader(CLIENT_IP_HEADER_NAME, clientIp);
        if (metadata == null) {
            return;
        }
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }
    }

    private void sendRequest(HttpRequestBase requestBase) throws IOException {
        CloseableHttpClient httpClient = getHttpClent();
        if (httpClient == null) {
            return;
        }
        httpClient.execute(requestBase, new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws IOException {
                try {
                    if (200 != response.getStatusLine().getStatusCode()) {
                        reportDecider.markUnpassed();
                        handleErrorResponse(response);
                    } else {//success
                        reportDecider.markPassed();
                        logger.debug(">> report to lookout agent ok.");
                    }
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                return null;
            }
        });
    }

    public void handleErrorResponse(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        Header header = response.getFirstHeader("Err");
        String errMsg = (header != null && header.getValue() != null) ? header.getValue() : "";
        if (401 == status) {
            logger.info(">>WARNING: Unauthorized!msg:{}", errMsg);
        } else if (403 == status) {
            logger.info(">>WARNING: Forbidden!msg:{}", errMsg);
        } else if (555 == status) {
            logger.info(">>WARNING: agent current limit!msg:{}", errMsg);
        } else {
            logger.info(">>WARNING: report to lookout agent fail!status:{}!msg:{}", status, errMsg);
        }
        Registry r = Lookout.registry();
        r.counter(r.createId(LOOKOUT_REPORT_FAIL_COUNT_ID).withTag(LOW_PRIORITY_TAG)).inc();

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
        reportDecider.changeSilentTime(wait, TimeUnit.MINUTES);
    }

    static CloseableHttpClient getHttpClent() {
        if (httpClientCache != null) {
            return httpClientCache;
        }
        if (httpClientInitialized.compareAndSet(false, true)) {
            BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager();
            connManager.closeIdleConnections(60, TimeUnit.SECONDS);
            httpClientCache = HttpClientBuilder.create().setConnectionManager(connManager)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(1, false))
                .setUserAgent(CLIENT_VERSION).build();
            return httpClientCache;
        }
        return null; //发生并发初始化情况;
    }

    static RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(1000).setSocketTimeout(1000).build();
    }

    @Override
    public void sendGetRequest(HttpGet httpGet, Map<String, String> metadata) throws IOException {
        addCommonHeaders(httpGet, metadata);
        httpGet.setConfig(reqConf);
        sendRequest(httpGet);
    }
}
