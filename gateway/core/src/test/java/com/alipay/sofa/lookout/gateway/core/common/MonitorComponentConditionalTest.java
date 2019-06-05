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
package com.alipay.sofa.lookout.gateway.core.common;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-13 22:58
 **/
public class MonitorComponentConditionalTest {

    @Test
    public void testgetConfigurationPhase() {
        MonitorComponentConditional m = new MonitorComponentConditional();
        Assert.assertEquals(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION,
            m.getConfigurationPhase());
    }

    @Test
    public void testMatch() {
        MonitorComponentConditional m = new MonitorComponentConditional();
        ConditionContext context = Mockito.mock(ConditionContext.class);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("components.active", "metrics");
        when(context.getEnvironment()).thenReturn(env);

        AnnotatedTypeMetadata atm = Mockito.mock(AnnotatedTypeMetadata.class);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap();
        map.put("value", Lists.newArrayList("metrics"));

        when(atm.getAllAnnotationAttributes(anyString())).thenReturn(map);
        Assert.assertTrue(m.matches(context, atm));
    }
}
