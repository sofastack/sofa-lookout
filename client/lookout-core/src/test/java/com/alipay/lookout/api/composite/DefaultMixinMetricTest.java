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
package com.alipay.lookout.api.composite;

import com.alipay.lookout.api.*;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by kevin.luy@alipay.com on 2017/1/25.
 */
public class DefaultMixinMetricTest {
    static Registry registry = new DefaultRegistry();
    static Id       basicId;

    @BeforeClass
    public static void init() {

        Id id = registry.createId("rpcServerInvokeStats");
        basicId = id.withTag("interfaceName", "demoService").withTag("methodName", "sayHello")
            .withTag("providerIp", "10.1.1.1").withTag("providerPort", "4030")
            .withTag("consumerAppId", "113").withTag("protocol", "tr").withTag("alias", "group1");
    }

    @Test
    public void testComponentGauge() {

        Id id = registry.createId("rpcServerThreadPool");

        MixinMetric mixinMetric = registry.mixinMetric(id);
        mixinMetric.gauge("threadNum", new Gauge() {
            @Override
            public Integer value() {
                return 3;
            }
        });

        MixinMetric metric = (MixinMetric) registry.get(id);
        // TODO, NEED? GET OR REMOVE??
        //        int threadNums = metric.<Gauge<Integer>>component("threadNum").value();
        //        System.out.println(threadNums);

    }

    @Test
    public void testRpcInvokeMetric() {

        invokeFromConsumerIP_1();
        invokeFromConsumerIP_2();

        Id id1 = basicId.withTag("consumerIp", "10.1.1.1").withTag("methodName", "sayHi");
        Id id2 = basicId.withTag("consumerIp", "20.2.2.2");

        MixinMetric invoke1 = (MixinMetric) registry.get(id1);
        MixinMetric invoke2 = (MixinMetric) registry.get(id2);

        assertEquals(invoke1.timer("perf").totalTime(), 2000);
        assertEquals(invoke2.timer("perf").totalTime(), 4000);
        assertEquals(invoke1.distributionSummary("inputSize").totalAmount(), 1024);
        assertEquals(invoke2.distributionSummary("outputSize").totalAmount(), 2048);
        assertNotEquals(invoke2.distributionSummary("inputSize").totalAmount(), 1024);

    }

    private void invokeFromConsumerIP_1() {

        Id id = basicId.withTag("consumerIp", "10.1.1.1")//newTag
            .withTag("methodName", "sayHi");//overrideTag

        //generate
        MixinMetric rpcServiceMetric = registry.mixinMetric(id);

        Timer rpcTimer = rpcServiceMetric.timer("perf");

        DistributionSummary rpcInSizeMetric = rpcServiceMetric.distributionSummary("outputSize");

        DistributionSummary rpcOutSizeMetric = rpcServiceMetric.distributionSummary("inputSize");
        for (int i = 0; i < 2; i++) {
            rpcTimer.record(1, TimeUnit.SECONDS);
            rpcInSizeMetric.record(512);
            rpcOutSizeMetric.record(512);
        }
    }

    private void invokeFromConsumerIP_2() {

        Id id = basicId.withTag("consumerIp", "20.2.2.2");//newTag
        //generate
        MixinMetric rpcServiceMetric = registry.mixinMetric(id);

        Timer rpcTimer = rpcServiceMetric.timer("perf");

        DistributionSummary rpcInSizeMetric = rpcServiceMetric.distributionSummary("outputSize");

        DistributionSummary rpcOutSizeMetric = rpcServiceMetric.distributionSummary("inputSize");
        for (int i = 0; i < 2; i++) {
            rpcTimer.record(2, TimeUnit.SECONDS);
            rpcInSizeMetric.record(1024);
            rpcOutSizeMetric.record(1024);
        }
    }

}
