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
package com.alipay.sofa.lookout.server.prom.labels;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-23 11:52
 **/
public class LabelsTest {

    @Test
    public void testAddLabel() {
        Labels labels = new Labels();
        labels.set("k1", "v1");
        Assert.assertEquals(1, labels.len());
        labels.add(new Label("k2", "v2"));
        Assert.assertEquals(2, labels.len());
    }

    @Test
    public void testRemoveLabelByLabelName() {
        Labels labels = new Labels();
        labels.set("k1", "v1");
        labels.add(new Label("k2", "v2"));
        Assert.assertEquals(2, labels.len());

        labels.del("k1");
        Assert.assertEquals(1, labels.len());
        Assert.assertFalse(labels.toString().contains("k1"));
    }

    @Test
    public void testRemoveLabelByLabes() {
        Labels labels = new Labels();
        labels.set("k1", "v1");
        labels.add(new Label("k2", "v2"));
        Assert.assertEquals(2, labels.len());
        labels.del(Lists.newArrayList("k1", "k2"));
        Assert.assertEquals(0, labels.len());
    }

    @Test
    public void testRemoveLabel() {
        Labels labels = new Labels();
        labels.set("k1", "v1");
        Assert.assertEquals(1, labels.len());
        labels.remove(new Label("k1", "v1"));
        Assert.assertEquals(0, labels.len());
    }

    @Test
    public void testGetLabelValue() {
        Labels labels = new Labels();
        labels.set("k1", "v1");
        Assert.assertEquals("v1", labels.getValue("k1"));
    }

    @Test
    public void testGetLabels() {
        Labels labels = new Labels();
        labels.set("k1", "v1");
        labels.set("k2", "v2");
        labels.set("k3", "v3");
        labels.set("k3", "v4");
        StringBuilder sb=new StringBuilder();
        labels.getLabels().stream().forEach(x->sb.append(x.toString()));
        System.out.println(sb.toString());
        Assert.assertEquals("k1=v1k2=v2k3=v3",sb.toString());

    }
}
