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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.queue;

import com.alipay.lookout.api.NoopRegistry;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 23:26
 **/
public class MetricsPersistQueueProcessorTest {

    @Before
    public void init() {
        File file = new File(getTmpPath());
        if (file.exists()) {
            for (File f : file.listFiles()) {
                f.delete();
            }
        }
    }

    public String getTmpPath() {
        return System.getProperty("java.io.tmpdir") + "test/";
    }

    @Test
    public void testQueue() {
        MetricsPersistQueueProcessor processor = new MetricsPersistQueueProcessor(5, getTmpPath(),
            "test1", NoopRegistry.INSTANCE);
    }
}
