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
package com.alipay.lookout.remote;

import com.alipay.lookout.api.*;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/5/15.
 */
public class LookoutMeasurementTest {

    static List<LookoutMeasurement> measurements;
    static Registry                 registry = new DefaultRegistry();
    static Id                       id       = registry.createId("aaaaaa.bbbbbbb.ccccc");

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
    public void testLookoutMeasurementFromMetric() {
        Counter counter = registry.counter(registry.createId("a.b.c"));

        LookoutMeasurement measurement = LookoutMeasurement.from(counter, null);
        Assert.assertTrue("c".equals(measurement.getTags().get("_type_")));

        DistributionSummary ds = registry.distributionSummary(registry.createId("a.b.d"));
        measurement = LookoutMeasurement.from(ds, null);
        Assert.assertTrue("d".equals(measurement.getTags().get("_type_")));

        Timer timer = registry.timer(registry.createId("a.b.t"));
        measurement = LookoutMeasurement.from(timer, null);
        Assert.assertTrue("t".equals(measurement.getTags().get("_type_")));

    }

}
