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
package com.alipay.lookout.common;

import com.alipay.lookout.api.Metric;
import com.alipay.lookout.common.utils.MetricsUtil;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by kevin.luy@alipay.com on 2017/5/15.
 */
public class MetricsUtilTest {

    @Test
    public void testSortedIterator() {
        DefaultRegistry registry = new DefaultRegistry();
        registry.counter(registry.createId("ebc"));
        registry.counter(registry.createId("dxy"));
        registry.counter(registry.createId("att"));

        Iterator<Metric> newIt = MetricsUtil.sortedIterator(registry.iterator(),
            new Comparator<Metric>() {
                @Override
                public int compare(Metric o1, Metric o2) {
                    return o1.id().name().compareTo(o2.id().name());
                }
            });

        while (newIt.hasNext()) {
            System.out.println(newIt.next().id().name());
        }

    }
}
