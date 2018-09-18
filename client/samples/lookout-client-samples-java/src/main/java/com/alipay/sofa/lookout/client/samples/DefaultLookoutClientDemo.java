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
package com.alipay.sofa.lookout.client.samples;

import com.alipay.lookout.api.*;
import com.alipay.lookout.client.DefaultLookoutClient;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.MetricObserver;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2018/4/23.
 */
public class DefaultLookoutClientDemo {
    public static void main(String[] args) {
        //构建一个全局的客户端实例
        final DefaultLookoutClient client = new DefaultLookoutClient("appName");

        LookoutConfig lookoutConfig=new LookoutConfig();

        //主动上报型
        //向远程 Agent(gateway）server上报metrics,必须的设置;
        //lookoutConfig.setProperty(LookoutConfig.LOOKOUT_AGENT_HOST_ADDRESS,"10.101.92.239");
        LookoutRegistry lookoutRegistry = new LookoutRegistry(lookoutConfig);
        //set common tag example
        lookoutRegistry.setCommonTag("instant", "machine-a");

        //本地观察metrics定时打印(optional)
        lookoutRegistry.addMetricObserver(new StdoutObserver());

        //可以 add 多个不同类型的 Registry 实例
        client.addRegistry(lookoutRegistry);

        //注册扩展的metrics，下面两种方式都可以
        //lookoutRegistry.registerExtendedMetrics();
        client.registerExtendedMetrics();

        Registry registry = client.getRegistry();

        //具体使用示例
        Id id = registry.createId("http_requests_total");
        Counter counter = registry.counter(id);
        counter.inc();

        testDistributionSummary(registry);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testDistributionSummary(Registry registry) {
        Id id = registry.createId("rpc_latency_distribution").withTag("service", "orderService");
        final Timer timer = registry.timer(id);
        timer.buckets(new long[] {10, 100, 1000, 10000});
        new Thread(new Runnable() {
            public void run() {
                final Random random = new Random();
                for (int i = 0; i < 10000; i++) {
                    timer.record(random.nextInt(200),TimeUnit.MILLISECONDS);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                final Random random = new Random();
                for (int i = 0; i < 1000; i++) {
                    timer.record(random.nextInt(2000),TimeUnit.MILLISECONDS);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                final Random random = new Random();
                for (int i = 0; i < 100; i++) {
                    timer.record(random.nextInt(20000),TimeUnit.MILLISECONDS);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    static class StdoutObserver implements MetricObserver<LookoutMeasurement> {

        public StdoutObserver() {
        }

        public boolean isEnable() {
            return true;
        }

        public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
            if (!measures.isEmpty()) {
                System.out.println("------------------------------------------");
                for (LookoutMeasurement measurement : measures) {
                    System.out.println(measurement);
                }
            }
        }
    }
}
