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
package com.alipay.lookout.top;

import com.alipay.lookout.api.BasicTag;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.NoopRegistry;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.top.TopGauger;
import com.alipay.lookout.common.top.TopUtil;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/3/31.
 */
public class TopUtilTest {

    @Test
    public void testDescTopGauge() throws NoSuchFieldException, IllegalAccessException,
                                  InterruptedException {

        Registry registry = new DefaultRegistry();

        TopGauger topGauger = TopUtil.topGauger(registry, registry.createId("top5sql"), 2);

        topGauger.record(1000l, new BasicTag("sql1", "select1"));
        topGauger.record(2000l, new BasicTag("sql2", "select2"));
        topGauger.record(3000l, new BasicTag("sql3", "select3"));

        //wait a sec
        Field field = TopUtil.class.getDeclaredField("executor");
        field.setAccessible(true);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) field.get(null);
        executor.awaitTermination(1, TimeUnit.SECONDS);

        Iterator<Metric> it = registry.iterator();
        String s = "";
        int i = 0;
        while (it.hasNext()) {
            i++;
            s += it.next().id();
        }
        Assert.assertEquals(2, i);
        Assert.assertTrue(s.contains("select2"));
        Assert.assertTrue(s.contains("select3"));
    }

    @Test
    public void testAscTopGauge() throws NoSuchFieldException, IllegalAccessException,
                                 InterruptedException {

        Registry registry = new DefaultRegistry();

        TopGauger topGauger = TopUtil.topGauger(registry, registry.createId("top3sql"), 2,
            TopUtil.Order.ASC);

        topGauger.record(1000l, new BasicTag("sql1", "select1"));
        topGauger.record(2000l, new BasicTag("sql2", "select2"));
        topGauger.record(3000l, new BasicTag("sql3", "select3"));

        //wait a sec
        Field field = TopUtil.class.getDeclaredField("executor");
        field.setAccessible(true);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) field.get(null);

        executor.awaitTermination(1, TimeUnit.SECONDS);

        Iterator<Metric> it = registry.iterator();
        String s = "";
        int i = 0;
        while (it.hasNext()) {
            i++;
            s += it.next().id();
        }
        System.out.println(s);
        Assert.assertEquals(2, i);
        Assert.assertTrue(s.contains("select2"));
        Assert.assertTrue(s.contains("select1"));
    }

    @Test
    public void testTopGaugeWithNoopId() {
        TopGauger topGauger = TopUtil.topGauger(new DefaultRegistry(),
            NoopRegistry.INSTANCE.createId("xx"), 4);
        Assert.assertTrue(topGauger.getClass().getName().contains("NoopTopGauger"));
    }

}
