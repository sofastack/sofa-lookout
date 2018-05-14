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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kevin.luy@alipay.com on 2018/4/1.
 */
public class TagSetTest {

    @Test
    public void testHashcodeNotEqual() {
        TagSet raw = new TagSet(new TreeMap<String, Tag>());
        TagSet tagSet1 = raw.add("k1", "v1");
        TagSet tagSet2 = raw.add("k1", "v2");
        System.out.println(tagSet1.toString() + " hc " + tagSet1.hashCode());
        System.out.println(tagSet2.toString() + " hc " + tagSet2.hashCode());
        Assert.assertNotEquals(tagSet1.hashCode(), tagSet2.hashCode());
    }

    @Test
    public void testHashcodeEqual() {
        TagSet raw = new TagSet(new TreeMap<String, Tag>());
        TagSet tagSet1 = raw.add("k1", "v1").add("k", "v");
        TagSet tagSet2 = raw.add("k", "v").add("k1", "v1");
        Assert.assertEquals(tagSet1.hashCode(), tagSet2.hashCode());
    }

    @Test
    public void testNotEquals() {
        TagSet raw = new TagSet(new TreeMap<String, Tag>());
        TagSet tagSet1 = raw.add("k1", "v1");
        TagSet tagSet2 = raw.add("k1", "v2");
        System.out.println(tagSet1.toString() + " hc " + tagSet1.hashCode());
        System.out.println(tagSet2.toString() + " hc " + tagSet2.hashCode());
        Assert.assertNotEquals(tagSet1, tagSet2);
    }

    @Test
    public void testEquals() {
        TagSet raw = new TagSet(new TreeMap<String, Tag>());
        TagSet tagSet1 = raw.add("k1", "v1").add("k", "v");
        TagSet tagSet2 = raw.add("k", "v").add("k1", "v1");
        Assert.assertEquals(tagSet1, tagSet2);
    }

    @Test
    public void testTostring() {
        TagSet raw = new TagSet(new TreeMap<String, Tag>());
        TagSet tagSet1 = raw.add("k1", "v1");
        //覆盖掉v1
        tagSet1 = tagSet1.addAll(new String[] { "k2", "v2", "k1", "v3" });
        System.out.println(tagSet1.toString());
        Assert.assertTrue(tagSet1.toString().contains("k1=v3"));
    }

    @Test
    public void testAddAll() {
        TagSet raw = new TagSet(new TreeMap<String, Tag>());
        TagSet ts1 = raw.add(new BasicTag("k1", "k1")).addAll(new Tag[] {})
            .addAll(new ArrayList<Tag>()).addAll(new HashMap<String, String>());
        System.out.println(ts1);
        Assert.assertEquals("[k1=k1]", ts1.toString());
        Tag tag1 = new BasicTag("k1", "v1");
        Tag tag2 = new BasicTag("k2", "v2");
        Tag tag3 = new BasicTag("k3", "v3");
        Map<String, String> map = Maps.newTreeMap();
        map.put("k3", "v33");
        map.put("k4", "v4");
        TagSet ts2 = raw.addAll(new Tag[] { tag1, tag2 })
            .addAll(Lists.<Tag> newArrayList(tag2, tag3)).addAll(map);
        System.out.println(ts2.toString());
        Assert.assertEquals("[k1=v1, k2=v2, k3=v33, k4=v4]", ts2.toString());

    }

}
