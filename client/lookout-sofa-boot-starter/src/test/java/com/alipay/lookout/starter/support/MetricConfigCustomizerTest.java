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

import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.core.config.MetricConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void testMetricConfigCustomizer() {
        Assert.assertEquals("testbb", lookoutConfig.getString("testaa"));
    }
}
