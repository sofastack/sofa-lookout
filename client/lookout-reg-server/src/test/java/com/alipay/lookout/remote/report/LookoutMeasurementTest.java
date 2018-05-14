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
package com.alipay.lookout.remote.report;

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.api.*;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.GaugeWrapper;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.remote.step.MockClock;
import com.alipay.lookout.report.LogObserver;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * Created by kevin.luy@alipay.com on 2017/3/17.
 */
public class LookoutMeasurementTest {

    static List<LookoutMeasurement> measurements;
    static Registry                 registry = new DefaultRegistry();
    static Id                       id       = registry.createId("aaaaaa.bbbbbbb.ccccc");
    static MockClock                clock    = new MockClock();

    @BeforeClass
    public static void init() {

        measurements = new ArrayList<LookoutMeasurement>();
        for (int i = 0; i < 1000; i++) {
            LookoutMeasurement measurement = new LookoutMeasurement(new Date(), id);
            measurement.addTag("lsjdfljalsjdfljsalflsajfdljslfjlja" + i, "xxxxxxxxxxxx" + i);
            measurement.addTag("woeurowuouroqwuroquoeruoqweuroquro" + i, "xxxxxxxxxxxx" + i);
            measurement.addTag("xvjsdiuhsdifhsihfdsihfisdbsduisdhgish" + i, "xxxxxxxxxxxx" + i);
            measurement.addTag("qweeh,fhe,whfqwhefwhfqwefhw,efhwehf,wefh" + i, "xxxxxxxxxxxx" + i);
            measurement.addTag("zhisdhifhaisdhfiahsdihfiasdhfiahfihaif" + i, "xxxxxxxxxxxx" + i);
            measurement.put("xxx" + i, i);
            measurement.put("yyy" + i, i);
            measurement.put("zzz" + i, i);
            measurements.add(measurement);
        }

    }

    @Test
    public void testPrintTimer() {
        LookoutRegistry lookoutRegistry = new LookoutRegistry(clock, new LogObserver(),
            new LookoutConfig());
        com.alipay.lookout.api.Timer timer = lookoutRegistry.timer(lookoutRegistry.createId(
            "m_name").withTag("k1", "v1"));
        LookoutMeasurement m = LookoutMeasurement.from(timer, null);
        System.out.println(m.toString());
        Assert.assertEquals(3, m.getValues().size());
    }

    @Test
    public void testPrintTDistributionSummary() {
        LookoutRegistry lookoutRegistry = new LookoutRegistry(clock, new LogObserver(),
            new LookoutConfig());
        DistributionSummary summary = lookoutRegistry.distributionSummary(lookoutRegistry.createId(
            "m_name").withTag("k1", "v1"));
        LookoutMeasurement m = LookoutMeasurement.from(summary, null);
        System.out.println(m.toString());
        Assert.assertEquals(3, m.getValues().size());
    }

    @Test
    public void testPrintCounter() {
        LookoutRegistry lookoutRegistry = new LookoutRegistry(clock, new LogObserver(),
            new LookoutConfig());
        Counter counter = lookoutRegistry.counter(lookoutRegistry.createId("m_name").withTag("k1",
            "v1"));
        LookoutMeasurement m = LookoutMeasurement.from(counter, null);
        System.out.println(m.toString());
        Assert.assertEquals(2, m.getValues().size());

    }

    @Test
    public void testPrintGauge() {
        LookoutRegistry lookoutRegistry = new LookoutRegistry(clock, new LogObserver(),
            new LookoutConfig());
        Id id = lookoutRegistry.createId("m_name").withTag("k1", "v1");
        GaugeWrapper gaugeWrapper = new GaugeWrapper(id, new Gauge<Number>() {
            @Override
            public Number value() {
                return 99;
            }
        }, clock);
        LookoutMeasurement m = LookoutMeasurement.from(gaugeWrapper, null);
        System.out.println(m.toString());
        Assert.assertEquals(1, m.getValues().size());

    }

    @Test
    public void testManuJson() {
        Date date = new Date();
        LookoutMeasurement measurement = new LookoutMeasurement(date, id);
        int i = 3;
        measurement.addTag("lsjdfljalsjdfljsalflsajfdljslfjlja" + i, "xxxxxxxxxxxx" + i);
        measurement.put("xxx" + i, i);
        measurement.put("yyy" + i, i);
        measurement.put("zzz" + i, i);

        String str = null;
        long t1 = System.currentTimeMillis();
        str = measurement.toString();
        long t2 = System.currentTimeMillis();
        System.out.println("t2-t1:" + (t2 - t1));
        System.out.println(str);

        String jsonStr = "\"lsjdfljalsjdfljsalflsajfdljslfjlja3\":\"xxxxxxxxxxxx3\"},\"aaaaaa.bbbbbbb.ccccc\":{\"xxx3\":3,\"yyy3\":3,\"zzz3\":3}}";
        System.out.println(jsonStr);
        Assert.assertTrue(str.contains(jsonStr));
    }

    @Test
    public void testJsonPerf() {
        long t1 = System.currentTimeMillis();
        for (LookoutMeasurement measurement : measurements) {
            measurement.toString();
        }
        long t2 = System.currentTimeMillis();
        System.out.println("t2-t1:" + (t2 - t1));
        t2 = System.currentTimeMillis();
        for (LookoutMeasurement measurement : measurements) {
            JSON.toJSONString(measurement);
        }
        long t3 = System.currentTimeMillis();
        System.out.println("t3-t2:" + (t3 - t2));
    }

    static class OldLookoutMeasurement extends LinkedHashMap<String, Object> {
        private final Map<String, String> tags;

        public OldLookoutMeasurement(Date date) {
            this.put("time", date);
            this.tags = new HashMap<String, String>();
            this.put("tags", tags);
        }

        public void addTag(String tagName, String tagValue) {
            tags.put(tagName, tagValue);
        }

        public boolean containsTag(String key) {
            return tags.containsKey(key);
        }

        public Map<String, String> getTags() {
            return tags;
        }

    }
}
