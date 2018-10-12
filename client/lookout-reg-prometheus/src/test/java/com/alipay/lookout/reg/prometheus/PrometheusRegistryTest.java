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
package com.alipay.lookout.reg.prometheus;

import com.alipay.lookout.core.config.LookoutConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static com.alipay.lookout.reg.prometheus.ExporterServerTest.sendHttpRequest;

/**
 * Created by kevin.luy@alipay.com on 2018/5/11.
 */
public class PrometheusRegistryTest {
    private static PrometheusRegistry r;

    public PrometheusRegistryTest() {
    }

    @BeforeClass
    public static void init() {
        LookoutConfig lookoutConfig = new LookoutConfig();
        lookoutConfig.setProperty(LookoutConfig.APP_NAME, "appNameTest");
        r = new PrometheusRegistry(lookoutConfig);
        r.registerExtendedMetrics();
    }

    @Test
    public void testPromReg() throws IOException {
        String result = sendHttpRequest(new URL("http://localhost:9494/metrics"));
        Assert.assertTrue(result.contains("lookout.reg_max.size"));
    }

    @Test
    public void testPromRegAppTag() throws IOException {
        String result = sendHttpRequest(new URL("http://localhost:9494/metrics"));
        Assert.assertTrue(result.contains("appNameTest"));
    }
}
