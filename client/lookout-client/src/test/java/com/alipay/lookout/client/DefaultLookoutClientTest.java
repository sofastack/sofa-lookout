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

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.LookoutRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2017/10/4.
 */
public class DefaultLookoutClientTest {

    @Test(expected = IllegalStateException.class)
    public void testDefaultLookoutClientWithoutRegistry() throws Exception {
        LookoutClient client1 = null;
        LookoutClient client2 = null;
        try {
            client1 = new DefaultLookoutClient("demo");
            client2 = new DefaultLookoutClient("demo");
            client1.getRegistry();
        } finally {
            client1.close();
            client2.close();
        }

    }

    @Test
    public void testDefaultLookoutClient_addRegsitry() throws Exception {
        LookoutConfig lookoutConfig = new LookoutConfig();
        DefaultLookoutClient client = new DefaultLookoutClient("demo");
        LookoutRegistry lookoutRegistry = new LookoutRegistry(lookoutConfig);
        client.addRegistry(lookoutRegistry);
        client.addRegistry(new DefaultRegistry(lookoutConfig));
        Assert.assertEquals(2, ((CompositeRegistry) client.getRegistry()).getRegistries().size());
        client.close();
    }

    @Test
    public void testDefaultLookoutClient_addExtMetrics() throws Exception {
        LookoutConfig lookoutConfig = new LookoutConfig();
        DefaultLookoutClient client = new DefaultLookoutClient("demo");
        MetricRegistry r = new DefaultRegistry(lookoutConfig);
        client.addRegistry(r);
        client.registerExtendedMetrics();
        Id id = r.createId("jvm.gc");
        Assert.assertNotNull(client.getRegistry().get(id));
        client.close();
    }
}
