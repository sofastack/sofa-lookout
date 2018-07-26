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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.report.xflush.Listener;
import com.alipay.lookout.remote.report.xflush.XFlushHttpExporter;
import com.alipay.lookout.remote.report.xflush.SettableStepRegistry;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.remote.report.xflush.PollerController;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/17
 */
public class XFlushHttpExporterTest {
    @Ignore
    @Test
    public void test() throws IOException, InterruptedException {
        LookoutConfig config = new LookoutConfig();

        final LookoutRegistry lookoutRegistry = new LookoutRegistry(config);
        lookoutRegistry.registerExtendedMetrics();
        SettableStepRegistry dr = new SettableStepRegistry(Clock.SYSTEM, config);

        SimpleLookoutClient client = new SimpleLookoutClient("foo", lookoutRegistry, dr);
        dr.registerExtendedMetrics();

        PollerController fc = new PollerController(dr);

        XFlushHttpExporter e = new XFlushHttpExporter(fc, config);
        e.addListener(new Listener() {
            @Override
            public void onActive() {
                lookoutRegistry.getMetricObserverComposite().setEnabled(false);
                System.out.println("active");
            }

            @Override
            public void onIdle() {
                lookoutRegistry.getMetricObserverComposite().setEnabled(true);
                System.out.println("idle");
            }
        });
        e.start();

        Registry r = client.getRegistry();
        Id id = r.createId("sdf");
        Counter counter = r.counter(id);
        for (int i = 0; i < 10000; ++i) {
            counter.inc();
            System.out.println("inc");
            Thread.sleep(1000);
        }
        Thread.sleep(999999);
    }
}