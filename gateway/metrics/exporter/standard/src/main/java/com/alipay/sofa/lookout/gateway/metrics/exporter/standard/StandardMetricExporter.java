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
package com.alipay.sofa.lookout.gateway.metrics.exporter.standard;

import com.alibaba.fastjson.JSONObject;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.utils.NetworkUtil;
import com.alipay.sofa.lookout.gateway.core.common.Constants;
import com.alipay.sofa.lookout.gateway.core.common.DataType;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.AbstractBatchExporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.google.common.base.Preconditions;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出到另外一个gateway, 序列化方式和 StandardMetricImporter 是相反的
 *
 * @author xiangfeng.xzc
 * @date 2018/11/26
 */
public class StandardMetricExporter extends AbstractBatchExporter<Metric> {
    private static final Logger    LOGGER           = LoggerFactory
                                                        .getLogger(StandardMetricExporter.class);
    private static final String    CONTENT_ENCODING = "Content-Encoding";
    private static final String    SNAPPY           = "snappy";
    private static final String    APP_NAME         = "lookoutgateway";

    private static final MediaType MEDIA_TYPE_JSON  = MediaType.parse("application/json");
    private final OkHttpClient     client;
    private final String           url;
    private final boolean          snappyCompress;

    public StandardMetricExporter(Registry registry, String url, boolean snappyCompress) {
        super("standard", registry, 100, DataType.METRIC);
        this.url = Preconditions.checkNotNull(url);
        this.snappyCompress = snappyCompress;

        String ip = NetworkUtil.getLocalAddress().getHostAddress();
        client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                    .header(Constants.CLIENT_IP_HEADER_NAME, ip)
                    .header(Constants.APP_HEADER_NAME, APP_NAME)
                    // 表明这是一个来自gateway转发的请求, 也可以作为版本号使用 保证兼容性
                    .header("X-Lookout-Gateway", "1").build();
                return chain.proceed(request);
            }
        }).build();
    }

    @Override
    protected boolean flushList(List<Metric> list) throws Exception {
        Request request = createRequest(list);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.warn("fail to call another lookout-gateway response={}", response);
                return false;
            }
        }
        return true;
    }

    private Request createRequest(List<Metric> list) throws IOException {

        StringBuilder sb = new StringBuilder();
        for (Metric m : list) {
            String name = m.getName();

            int index = name.indexOf('.');
            if (index <= 0) {
                // 不应该
                continue;
            }

            JSONObject json = new JSONObject();
            json.put("time", m.getTimestamp());
            Map<String, String> tags = new HashMap<>(m.getTags());
            boolean isInfo = m.getInfo() != null;

            if (isInfo) {
                tags.put("_type_", "i");
            }
            json.put("tags", tags);

            String key = name.substring(0, index);
            String subKey = name.substring(index + 1);
            JSONObject sub = new JSONObject();
            if (isInfo) {
                sub.put(subKey, m.getInfo());
            } else {
                sub.put(subKey, m.getValue());
            }
            json.put(key, sub);

            sb.append(json.toString())
                    .append('\t');
        }

        String bodyStr = sb.toString();

        Request.Builder requestBuilder = new Request.Builder()
                .url(this.url);

        byte[] bytes = bodyStr.getBytes(StandardCharsets.UTF_8);
        if (snappyCompress) {
            requestBuilder.header(CONTENT_ENCODING, SNAPPY);
            bytes = Snappy.compress(bodyStr);
        }
        requestBuilder.post(RequestBody.create(MEDIA_TYPE_JSON, bytes));
        return requestBuilder.build();
    }

    @Override
    public boolean supports(DataType dataType) {
        return dataType == DataType.METRIC || dataType == DataType.METRIC_INFO;
    }
}
