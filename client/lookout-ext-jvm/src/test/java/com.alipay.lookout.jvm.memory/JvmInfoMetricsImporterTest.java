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
package com.alipay.lookout.jvm.memory;

import com.alipay.lookout.BasicMetricsImporter;
import com.alipay.lookout.FileSystemSpaceMetricsImporter;
import com.alipay.lookout.api.*;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.jvm.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class JvmInfoMetricsImporterTest {

    @Test
    public void testGcInfo() {
        Registry registry = new DefaultRegistry();
        JvmGcMetricsImporter jvmGcMetricsImporter = new JvmGcMetricsImporter();
        jvmGcMetricsImporter.register(registry);

        Id id = registry.createId("jvm.gc");
        MixinMetric mixin = registry.mixinMetric(id);
        Gauge gauge = mixin.gauge("young.count", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("young.time", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("old.count", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("old.time", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
    }

    @Test
    public void testMemInfo() {
        Registry registry = new DefaultRegistry();
        JvmMemoryMetricsImporter jvmMemoryMetricsImporter = new JvmMemoryMetricsImporter();
        jvmMemoryMetricsImporter.register(registry);

        Id id = registry.createId("jvm.memory");
        MixinMetric mixin = registry.mixinMetric(id);
        Gauge gauge = mixin.gauge("nonheap.init", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("nonheap.used", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("nonheap.committed", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("nonheap.max", null);
        System.out.println(gauge.value().longValue());
        Assert.assertTrue(gauge.value().longValue() >= -1);

        gauge = mixin.gauge("heap.init", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("heap.used", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("heap.committed", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("heap.max", null);
        Assert.assertTrue(gauge.value().longValue() >= -1);
    }

    @Test
    public void testThreadsInfo() {
        Registry registry = new DefaultRegistry();
        JvmThreadsMetricsImporter importer = new JvmThreadsMetricsImporter();
        importer.register(registry);

        Id id = registry.createId("jvm.threads");
        MixinMetric mixin = registry.mixinMetric(id);
        Gauge gauge = mixin.gauge("peak", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("daemon", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("totalStarted", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("active", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
    }

    @Test
    public void testSystemPropertiesInfo() {
        Registry registry = new DefaultRegistry();
        JvmSystemPropertiesInfoMetricImporter importer = new JvmSystemPropertiesInfoMetricImporter();
        importer.register(registry);

        Id id = registry.createId("jvm.system.properties");
        Info info = registry.info(id, null);
        Assert.assertNotNull(info.value());

        Id envId = registry.createId("jvm.system.env");
        Info info2 = registry.info(envId, null);
        Assert.assertNotNull(info2.value());
    }

    @Test
    public void testBaseInfo() {
        Registry registry = new DefaultRegistry();
        BasicMetricsImporter jvmGcMetricsImporter = new BasicMetricsImporter();
        jvmGcMetricsImporter.register(registry);

        Id id = registry.createId("instance");
        MixinMetric mixin = registry.mixinMetric(id);

        Gauge gauge = mixin.gauge("mem.total", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("mem.free", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("processors", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("uptime", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("systemload.average", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
    }

    @Test
    public void testJvmClassInfo() {
        Registry registry = new DefaultRegistry();
        JvmClassesMetricsImporter importer = new JvmClassesMetricsImporter();
        importer.register(registry);

        Id id = registry.createId("jvm.classes");
        MixinMetric mixin = registry.mixinMetric(id);

        Gauge gauge = mixin.gauge("unloaded", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("total", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
        gauge = mixin.gauge("loaded", null);
        Assert.assertTrue(gauge.value().longValue() >= 0);
    }

    @Test
    public void testFileSystemMetricsInfo() {
        Registry registry = new DefaultRegistry();
        FileSystemSpaceMetricsImporter jvmGcMetricsImporter = new FileSystemSpaceMetricsImporter();
        jvmGcMetricsImporter.register(registry);
        Iterator<Metric> iterator = registry.iterator();
        String ids = "";
        while (iterator.hasNext()) {
            Metric m = iterator.next();
            if (m instanceof MixinMetric) {
                MixinMetric mm = (MixinMetric) m;
                for (Object x : mm.measure().measurements()) {
                    ids += x.toString();
                }
            }
        }
        Assert.assertTrue(ids.contains("usabe.space"));
    }
}
