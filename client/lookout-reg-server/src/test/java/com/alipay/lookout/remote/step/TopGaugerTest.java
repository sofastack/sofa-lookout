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
package com.alipay.lookout.remote.step;

import com.alipay.lookout.api.BasicTag;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.common.top.TopGauger;
import com.alipay.lookout.common.top.TopUtil;
import com.alipay.lookout.remote.report.SchedulerPoller;
import com.alipay.lookout.spi.MetricFilter;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/4/5.
 */
public class TopGaugerTest {

    /**
     * top20,采集后能够重新统计新的top20
     */
    @Test
    public void testRefreshTopGauge() throws NoSuchFieldException, IllegalAccessException,
                                     InterruptedException, NoSuchMethodException,
                                     InvocationTargetException {
        MockClock clock = new MockClock();
        LookoutRegistry registry = LookoutRegistryTest.registry;

        TopGauger topGauger = TopUtil.topGauger(registry, registry.createId("top2sql"), 2);

        topGauger.record(1000l, new BasicTag("sql1", "select1"));
        topGauger.record(2000l, new BasicTag("sql2", "select2"));
        topGauger.record(3000l, new BasicTag("sql3", "select3"));
        //wait
        Thread.sleep(1000);
        //poll遍历一边；
        clock.setMonotonicTime(30000);

        SchedulerPoller poller = registry.poller();

        Method method = poller.getClass().getDeclaredMethod("getMeasurements", PRIORITY.class,
            MetricFilter.class);
        method.setAccessible(true);
        method.invoke(poller, null, null);

        //再更新top
        topGauger.record(9000l, new BasicTag("sql4", "select4"));
        topGauger.record(5000l, new BasicTag("sql5", "select5"));

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
            s += it.next().id() + "\n";
        }
        System.out.println(s);
        Assert.assertTrue(s.contains("select4"));
        Assert.assertTrue(s.contains("select5"));
        Assert.assertFalse(s.contains("select2"));
        Assert.assertFalse(s.contains("select3"));

    }

}
