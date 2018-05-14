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

import com.alipay.lookout.spi.DefaultMetricsImporterLocator;
import com.alipay.lookout.spi.MetricsImporter;
import com.alipay.lookout.spi.MetricsImporterLocator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class JvmMemoryMetricsImporterTest {

    @Test
    public void testServiceLocateIt() {
        MetricsImporterLocator locator = new DefaultMetricsImporterLocator();
        Collection<MetricsImporter> mis = locator.locate();
        System.out.println(mis);
        Assert.assertTrue(mis.size() > 0);
    }

    //    @Test
    //    public void testJvmMemoryMetricsImporter() {
    //        Registry r = new DefaultRegistry();
    //        JvmMemoryMetricsImporter jvmMemoryMetricsImporter = new JvmMemoryMetricsImporter();
    //        jvmMemoryMetricsImporter.register(r);
    //        System.out.println("s");
    //    }

    //    @Test
    //    public void testDISK() {
    //        File[] roots = File.listRoots();
    //
    //        /* For each filesystem root, print some info */
    //        for (File root : roots) {
    //            System.out.println("File system root: " + root.getAbsolutePath());
    //            System.out.println("Total space (bytes): " + root.getTotalSpace());
    //            System.out.println("Free space (bytes): " + root.getFreeSpace());
    //            System.out.println("Usable space (bytes): " + root.getUsableSpace());
    //        }
    //    }
}
