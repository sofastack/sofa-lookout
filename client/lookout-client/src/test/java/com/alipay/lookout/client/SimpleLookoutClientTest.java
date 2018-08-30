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

import com.alipay.lookout.api.BasicTag;
import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.NoopRegistry;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.LookoutRegistry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by kevin.luy@alipay.com on 2017/5/23.
 */
public class SimpleLookoutClientTest {

    @BeforeClass
    public static void init() throws NoSuchFieldException, IllegalAccessException {
        Field field = Lookout.class.getDeclaredField("atomicRegistryReference");
        field.setAccessible(true);
        AtomicReference<Registry> atomicRegistryReference = (AtomicReference<Registry>) field
            .get(null);
        atomicRegistryReference.set(NoopRegistry.INSTANCE);
    }

    /**
     * 该实例只能全局单例，所以就统一在一个测试方法
     */
    @Test
    public void testSimpleLookoutClient() throws Exception {
        LookoutRegistry lookoutRegistry = new LookoutRegistry(new LookoutConfig());
        SimpleLookoutClient client = new SimpleLookoutClient("demo", lookoutRegistry);
        //test addCommonTags
        client.addCommonTags(new BasicTag("k", "v"));
        Assert.assertEquals("v", lookoutRegistry.getCommonTagValue("k"));
        //test addDefaultCommonTags
        Assert.assertEquals("demo", lookoutRegistry.getCommonTagValue("app"));
        //test use same config
        Assert.assertSame(client.getLookoutConfig(), lookoutRegistry.getConfig());
        //test get Registry
        Assert.assertSame(lookoutRegistry, ((CompositeRegistry) client.getRegistry())
            .getRegistries().iterator().next());
        client.close();
    }

}
