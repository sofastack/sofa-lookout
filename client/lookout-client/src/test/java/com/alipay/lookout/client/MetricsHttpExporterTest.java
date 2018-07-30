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
package com.alipay.lookout.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.lookout.api.Clock;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.report.poller.Listener;
import com.alipay.lookout.remote.report.poller.MetricsHttpExporter;
import com.alipay.lookout.remote.report.poller.PollerController;
import com.alipay.lookout.remote.report.poller.SettableStepRegistry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/17
 */
public class MetricsHttpExporterTest {
    @Test
    public void test() throws IOException, InterruptedException {
        LookoutConfig config = new LookoutConfig();

        // final LookoutRegistry lookoutRegistry = new LookoutRegistry(config);
        // lookoutRegistry.registerExtendedMetrics();

        SettableStepRegistry dr = new SettableStepRegistry(Clock.SYSTEM, config, 1000L);
        dr.registerExtendedMetrics();

        // SimpleLookoutClient client = new SimpleLookoutClient("foo", lookoutRegistry, dr);
        // dr.registerExtendedMetrics();

        PollerController fc = new PollerController(dr);

        fc.addListener(new Listener() {
            @Override
            public void onActive() {
                // lookoutRegistry.getMetricObserverComposite().setEnabled(false);
                System.out.println("active");
            }

            @Override
            public void onIdle() {
                // lookoutRegistry.getMetricObserverComposite().setEnabled(true);
                System.out.println("idle");
            }
        });

        MetricsHttpExporter e = new MetricsHttpExporter(fc);
        e.start();

        Thread.sleep(1200);

        CloseableHttpClient hc = HttpClients.createDefault();
        try {
            HttpUriRequest request = RequestBuilder.get("http://localhost:19399/get").build();
            CloseableHttpResponse response = hc.execute(request);
            try {
                String content = EntityUtils.toString(response.getEntity());
                JSONObject r = JSON.parseObject(content);
                System.out.println(r);
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        } finally {
            HttpClientUtils.closeQuietly(hc);
            e.close();
        }
    }
}