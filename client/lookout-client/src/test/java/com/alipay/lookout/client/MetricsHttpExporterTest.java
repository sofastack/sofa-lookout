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
import com.alipay.lookout.remote.step.LookoutRegistry;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/17
 */
public class MetricsHttpExporterTest {
    private void bind(PollerController controller,
                      final Collection<LookoutRegistry> lookoutRegistries) {
        controller.addListener(new Listener() {
            @Override
            public void onActive() {
                for (LookoutRegistry r : lookoutRegistries) {
                    r.getMetricObserverComposite().setEnabled(false);
                }
                System.out.println("active");
            }

            @Override
            public void onIdle() {
                for (LookoutRegistry r : lookoutRegistries) {
                    r.getMetricObserverComposite().setEnabled(false);
                }
                System.out.println("idle");
            }
        });

    }

    @Test
    public void test() throws IOException, InterruptedException {
        LookoutConfig config = new LookoutConfig();
        final LookoutRegistry lookoutRegistry = new LookoutRegistry(Clock.SYSTEM, null, config,
            null, 1000L);
        // 通常只会有一个LookoutRegistry
        final Collection<LookoutRegistry> lookoutRegistries = new ArrayList<LookoutRegistry>(1);
        lookoutRegistries.add(lookoutRegistry);

        // 使用者需要自行构建该 PollerController
        // 能不能将逻辑做到 client 里?

        // Registry registry = client.getRegistry();

        PollerController pc = new PollerController(lookoutRegistry);
        bind(pc, lookoutRegistries);

        MetricsHttpExporter e = new MetricsHttpExporter(pc);
        e.start();

        // SimpleLookoutClient client = new SimpleLookoutClient("foo", config, lookoutRegistry, ssr);

        Thread.sleep(1200);

        CloseableHttpClient hc = HttpClients.createDefault();
        try {
            HttpClientUtils.closeQuietly(hc.execute(RequestBuilder.get(
                "http://localhost:19399/clear").build()));
            HttpUriRequest request = RequestBuilder.get("http://localhost:19399/get?success=1,2,3")
                .build();
            CloseableHttpResponse response = hc.execute(request);
            try {
                String content = EntityUtils.toString(response.getEntity());
                JSONObject r = JSON.parseObject(content);
                assertNotNull(r);
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        } finally {
            HttpClientUtils.closeQuietly(hc);
            e.close();
        }
    }
}