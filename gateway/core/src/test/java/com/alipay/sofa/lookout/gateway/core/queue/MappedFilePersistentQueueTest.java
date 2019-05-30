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
package com.alipay.sofa.lookout.gateway.core.queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 20:07
 **/
public class MappedFilePersistentQueueTest {

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
    public void testConsumeAndProduce() throws IOException {
        String filePath = getTmpPath();
        MappedFilePersistentQueue queue = new MappedFilePersistentQueue(filePath, "test1");
        String data = "hello world";
        byte[] in = data.getBytes();
        Assert.assertTrue(queue.produce(in));
        byte[] out = queue.consume();
        Assert.assertEquals(data, new String(out));
    }

    @Test
    public void testConsumeFail() throws IOException {
        String filePath = getTmpPath();
        MappedFilePersistentQueue queue = new MappedFilePersistentQueue(filePath, "test2");
        String data = "hello sofa1";
        byte[] in = data.getBytes();
        Assert.assertTrue(queue.produce(in));

        byte[] out = new byte[1];
        int length = queue.consume(out);
        //out size is too small
        Assert.assertEquals(0, length);
    }

    @Test
    public void testConsumeSuccess() throws IOException {
        String filePath = getTmpPath();
        MappedFilePersistentQueue queue = new MappedFilePersistentQueue(filePath, "test3");
        String data = "hello sofa12";
        byte[] in = data.getBytes();
        Assert.assertTrue(queue.produce(in));

        byte[] out2 = new byte[100];
        int length = queue.consume(out2);
        Assert.assertEquals(12, length);

    }
}
