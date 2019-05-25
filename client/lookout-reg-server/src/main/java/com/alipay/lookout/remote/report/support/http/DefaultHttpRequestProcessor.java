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
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.AddressService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.lookout.common.LookoutConstants.LOW_PRIORITY_TAG;
import static com.alipay.lookout.remote.report.SchedulerPoller.PRIORITY_NAME;

/**
 * 将数据推送到 lookout-gateway 上
 * Created by kevin.luy@alipay.com on 2017/4/13.
 */
public final class DefaultHttpRequestProcessor extends ReportDecider {
    private static final Logger        logger                       = LookoutLoggerFactory
                                                                        .getLogger(DefaultHttpRequestProcessor.class);

    public static final String         CLIENT_IP_HEADER_NAME        = "Client-Ip";
    public static final String         LOOKOUT_REPORT_FAIL_COUNT_ID = "lookout.report.fail";
    public static final String         WAIT_MINUTES                 = "Wait-Minutes";
    static final String                CLIENT_VERSION               = "LOOKOUT-CLIENT-V1";
    static final String                APP_HEADER_NAME              = "app";
    static final String                CONFIG_HEADER_NAME           = "Conf-Id";
    private static final String        CELL_HEADER_NAME             = "Cell";

    private final String               clientIp                     = NetworkUtil.getLocalAddress()
                                                                        .getHostAddress();

    final static RequestConfig         reqConf                      = buildRequestConfig();
    //HTTP
    static CloseableHttpClient         httpClientCache;
    private static Runnable            clearIdleConnectionsTask;

    private static final AtomicBoolean httpClientInitialized        = new AtomicBoolean(false);

    private final ReportConfigUtil     reportConfigUtil             = new ReportConfigUtil();

    public DefaultHttpRequestProcessor(AddressService addressService, MetricConfig metricConfig) {
        super(addressService, metricConfig);
    }

    @Override
    public boolean sendGetRequest(final HttpGet httpGet, Map<String, String> metadata)
                                                                                      throws IOException {
        return sendGetRequest(httpGet, metadata, new ResultConsumer() {
            @Override
            public void consume(HttpEntity entity) {
                logger.debug("check lookout gateway ok.{}", httpGet.toString());
                reportConfigUtil.getConfigResultConsumer().consume(entity);
            }
        });
    }

    @Override
    public boolean sendGetRequest(final HttpGet httpGet, Map<String, String> metadata,
                                  final ResultConsumer resultConsumer) throws IOException {
        //with conf-id
        httpGet.setHeader(CONFIG_HEADER_NAME, reportConfigUtil.getReportConfig().getId());
        // add cellinfo
        String zone = System.getProperty("com.alipay.ldc.zone");
        if (StringUtils.isNotEmpty(zone)) {
            httpGet.setHeader(CELL_HEADER_NAME, zone);
        }
        addCommonHeaders(httpGet, metadata);
        httpGet.setConfig(reqConf);
        return sendRequest(httpGet, new ResponseHandler<Boolean>() {
            @Override
            public Boolean handleResponse(HttpResponse response) throws IOException {
                if (response.containsHeader("Conf-Refresh")) {
                    resultConsumer.consume(response.getEntity());
                }
                try {
                    if (200 == response.getStatusLine().getStatusCode()) {
                        return true;
                    }
                    //client can not use this server address;
                    if (412 == response.getStatusLine().getStatusCode()) {
                        logger.debug("<< the address:{} is not recommended for this client use.",
                            httpGet);
                        return false;
                    }
                    if (200 != response.getStatusLine().getStatusCode()) {
                        handleErrorResponse(response, httpGet);
                        return false;
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
        if (metadata != null && PRIORITY.LOW.name().equalsIgnoreCase(metadata.get(PRIORITY_NAME))
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
                            logger.debug("report to lookout gateway ok.{}", httpPost.toString());
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

    private void addCommonHeaders(HttpRequestBase httpMtd, Map<String, String> metadata) {
        httpMtd.setHeader(CLIENT_IP_HEADER_NAME, clientIp);
        String app = getMetricConfig().getString(LookoutConfig.APP_NAME);
        if (StringUtils.isNotEmpty(app)) {
            httpMtd.setHeader(APP_HEADER_NAME, app);
        }
        if (metadata == null) {
            return;
        }
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            httpMtd.setHeader(entry.getKey(), entry.getValue());
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
        } else if (404 == status) {
            logger.debug(">>WARNING: ResourceNotFound!msg:{},request:{}", errMsg,
                request.toString());
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

    /**
     * lazy init singleton.
     *
     * @return CloseableHttpClient http client
     */
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

    @Override
    public List<LookoutMeasurement> filter(List<LookoutMeasurement> measures) {
        return reportConfigUtil.filter(measures);
    }
}
