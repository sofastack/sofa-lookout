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
package com.alipay.lookout.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2018/5/16.
 */
public class DefaultIdTest {

    @Test
    public void testEquals() {

        Id id1 = new DefaultId("name").withTag(new BasicTag("k1", "v1")).withTag(
            new BasicTag("k2", "v2"));

        Tag[] tags = new Tag[] { new BasicTag("k2", "v2"), new BasicTag("k1", "v1") };
        Id id2 = new DefaultId("name").withTags(tags);
        System.out.println(id1);
        Assert.assertTrue(id1.equals(id2));
        Assert.assertEquals(id1, id2);
        Assert.assertEquals(id1.hashCode(), id2.hashCode());

    }
}
