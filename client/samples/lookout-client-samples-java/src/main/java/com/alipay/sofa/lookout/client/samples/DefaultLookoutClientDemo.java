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

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.client.DefaultLookoutClient;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.MetricObserver;

import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2018/4/23.
 */
public class DefaultLookoutClientDemo {
    public static void main(String[] args) {
        //构建一个全局的客户端实例
        final DefaultLookoutClient client = new DefaultLookoutClient("appName");

        LookoutRegistry lookoutRegistry = new LookoutRegistry(new LookoutConfig());

        //set common tag example
        lookoutRegistry.setCommonTag("instant", "machine-a");

        //本地观察metrics定时打印
        lookoutRegistry.addMetricObserver(new StdoutObserver());
        client.addRegistry(lookoutRegistry);

        //注册扩展的metrics，下面两种方式都可以
        //lookoutRegistry.registerExtendedMetrics();
        client.registerExtendedMetrics();

        //具体使用示例
        Id id = client.getRegistry().createId("http_requests_total");
        Counter counter = client.getRegistry().counter(id);
        counter.inc();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    static class StdoutObserver implements MetricObserver<LookoutMeasurement> {

        public StdoutObserver() {
        }

        public boolean isEnable() {
            return true;
        }

        public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
            if (!measures.isEmpty())
                System.out.println("==> " + measures.toString());
        }
    }
}
