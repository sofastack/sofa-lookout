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

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.Timer;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.reg.prometheus.common.PromWriter;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class PromWriterTest {
    @Test
    public void testPrintPromText() {
        PromWriter promWriter = new PromWriter();
        Id id = new DefaultRegistry().createId("aa.bb.cc");

        Date date = new Date();
        LookoutMeasurement measurement = new LookoutMeasurement(date, id);
        int i = 3;
        measurement.addTag("tagk" + i, "tagv" + i);
        measurement.put("xxx" + i, i);
        measurement.put("yyy" + i, i);
        measurement.put("zzz" + i, i);

        String str = promWriter.printFromLookoutMeasurement(measurement);
        System.out.println(str);
        Assert.assertTrue(str.contains("aa.bb.cc_xxx3{tagk3=\"tagv3\"} 3"));
    }

    @Test
    public void testPrintPromTextFromDefaultRegistry() {
        PromWriter promWriter = new PromWriter();
        DefaultRegistry r = new DefaultRegistry();
        Id id = r.createId("aa.bb.cc").withTag("tagk1", "tagv1").withTag("tagk2", "tagv2");
        Timer timer = r.timer(id);
        timer.record(2, TimeUnit.SECONDS);
        Iterator<Metric> iterator = r.iterator();
        String str = "";
        while (iterator.hasNext()) {
            str += promWriter.printFromIndicator(iterator.next().measure());
        }
        System.out.println(str);
    }

    @Test
    public void testSnakeCase() {
        PromWriter promWriter = new PromWriter();
        Assert.assertEquals("aa_bb", promWriter.snakecase("aa.bb"));
        Assert.assertEquals("aabb", promWriter.snakecase("aabb"));
    }

    @Test
    public void testFormatePromMetricName() {
        PromWriter promWriter = new PromWriter();
        Assert.assertEquals("aa_bb", promWriter.formatMetricName("aa-bb"));
        Assert.assertEquals("aa_bb", promWriter.formatMetricName("aa.bb"));
        Assert.assertEquals("m__aa_bb", promWriter.formatMetricTagKey("_aa-bb"));

    }

    @Test
    public void testFormatePromMetricTagKey() {
        PromWriter promWriter = new PromWriter();
        Assert.assertEquals("m__aa_bb", promWriter.formatMetricTagKey("_aa-bb"));
        Assert.assertEquals("aa_bb", promWriter.formatMetricName("aa-bb"));
        Assert.assertEquals("aa_bb", promWriter.formatMetricName("aa.bb"));
    }
}
