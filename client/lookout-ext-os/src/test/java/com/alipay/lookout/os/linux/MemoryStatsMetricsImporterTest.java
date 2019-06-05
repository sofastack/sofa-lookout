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
package com.alipay.lookout.os.linux;

import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-03-12 21:40
 **/
public class MemoryStatsMetricsImporterTest {

    @Test
    public void test() {
        Registry registry = new DefaultRegistry();
        MemoryStatsMetricsImporter memoryStatsMetricsImporter = new MemoryStatsMetricsImporter(
            "src/test/resources/proc_meminfo", 1000, TimeUnit.MILLISECONDS);
        memoryStatsMetricsImporter.register(registry);
        Iterator<Metric> iterator = registry.iterator();
        while (iterator.hasNext()) {
            Metric metric = iterator.next();
            Collection<Measurement> measurements = metric.measure().measurements();
            Assert.assertEquals(measurements.size(), 4);
        }
    }
}
