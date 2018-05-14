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
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static com.alipay.lookout.reg.prometheus.ExporterServerTest.sendHttpRequest;

/**
 * Created by kevin.luy@alipay.com on 2018/5/11.
 */
public class PrometheusRegistryTest {

    @Test
    public void testPromReg() throws IOException {
        PrometheusRegistry r = new PrometheusRegistry(new LookoutConfig());
        r.registerExtendedMetrics();
        String result = sendHttpRequest(new URL("http://localhost:9494/metrics"));
        System.out.println(result);
        Assert.assertTrue(result.contains("lookout_reg_max_size"));
        r.close();
    }
}
