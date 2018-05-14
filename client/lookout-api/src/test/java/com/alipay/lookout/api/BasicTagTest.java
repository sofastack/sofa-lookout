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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BasicTagTest {

    @Test
    public void testTagEquality() {
        // NOTE: EqualsVerifier doesn't work with cached hash code
        final BasicTag tag1 = new BasicTag("k1", "v1");
        BasicTag tag2 = new BasicTag("k2", "v2");
        Assert.assertEquals(tag1, tag1);
        Assert.assertEquals(tag2, tag2);
        Assert.assertNotEquals(tag1, tag2);
        Assert.assertNotEquals(tag1, null);
        Assert.assertNotEquals(tag1, new Object());
        Assert.assertNotEquals(tag1, new BasicTag("k1", "v2"));
        Assert.assertNotEquals(tag1, new BasicTag("k2", "v1"));
        Assert.assertNotEquals(tag1, new Tag() {
            @Override
            public String key() {
                return tag1.key();
            }

            @Override
            public String value() {
                return tag1.value();
            }
        });
    }

    @Test
    public void testHashCode() {
        BasicTag tag = new BasicTag("k1", "v1");
        Assert.assertEquals(tag.hashCode(), new BasicTag(tag.key(), tag.value()).hashCode());
    }

    @Test
    public void testToString() {
        BasicTag tag = new BasicTag("k1", "v1");

        Assert.assertEquals("k1=v1", tag.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKey() {
        new BasicTag(null, "v");
    }

    // no exception
    @Test
    public void testNullValue() {
        new BasicTag("k", null);
    }
}
