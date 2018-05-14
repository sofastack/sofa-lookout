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
package com.alipay.lookout.remote.report.support;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/4/13.
 */
public class ReportDeciderTest {

    ReportDecider reportDecider = new ReportDecider();

    @Test
    public void testchangeSilentTime() {
        //初始态
        Assert.assertFalse(reportDecider.stillSilent());
        Assert.assertTrue(getSlientTime() <= 0);

        //目标形态
        long expectTime = System.currentTimeMillis() + 60000;
        //1分钟沉默
        reportDecider.changeSilentTime(1, TimeUnit.MINUTES);
        Assert.assertTrue(reportDecider.stillSilent());

        //改为2s，但不会生效，应该还是按照最大的1分钟为准；
        reportDecider.changeSilentTime(2, TimeUnit.SECONDS);
        Assert.assertTrue(getSlientTime() >= expectTime);
    }

    private long getSlientTime() {
        try {
            Field field = ReportDecider.class.getDeclaredField("silentTime");
            field.setAccessible(true);
            return field.getLong(reportDecider);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
