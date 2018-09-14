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
package com.alipay.lookout.starter.support;

import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.reg.prometheus.PrometheusRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * Created by kevin.luy@alipay.com on 2018/9/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MetricConfigCustomizerTest.class)
@Component
@ComponentScan
@SpringBootApplication
@Import(MetricConfigCustomizerConfig.class)
public class MetricConfigCustomizerTest {

    @Autowired
    LookoutConfig lookoutConfig;
    @AfterClass
    public static void close() throws IOException {
        CompositeRegistry reg = (CompositeRegistry) Lookout.registry();
        for (Registry r : reg.getRegistries()) {
            if (r instanceof PrometheusRegistry) {
                ((PrometheusRegistry) r).close();
            }
        }
    }
    @Test
    public void testMetricConfigCustomizer() {
        Assert.assertEquals("testbb", lookoutConfig.getString("testaa"));
    }
}
