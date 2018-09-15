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
package com.alipay.lookout.common.utils;

import com.alipay.lookout.api.BasicTag;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.api.Tag;
import com.alipay.lookout.common.LookoutConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.alipay.lookout.common.LookoutConstants.TAG_PRIORITY_KEY;

/**
 * Created by kevin.luy@alipay.com on 2018/5/15.
 */
public class PriorityTagUtilTest {

    @Test
    public void testResolveHigh() {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new BasicTag("k1", "v1"));
        tags.add(new BasicTag(TAG_PRIORITY_KEY, PRIORITY.HIGH.toString()));
        Assert.assertEquals(PRIORITY.HIGH, PriorityTagUtil.resolve(tags));
    }

    @Test
    public void testResolveHigh2() {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new BasicTag("k1", "v1"));
        tags.add(LookoutConstants.HIGH_PRIORITY_TAG);
        Assert.assertEquals(PRIORITY.HIGH, PriorityTagUtil.resolve(tags));
    }

    @Test
    public void testResolveNothing() {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new BasicTag("k1", "v1"));
        Assert.assertNotEquals(PRIORITY.HIGH, PriorityTagUtil.resolve(tags));
        Assert.assertEquals(PRIORITY.NORMAL, PriorityTagUtil.resolve(tags));

    }
}
