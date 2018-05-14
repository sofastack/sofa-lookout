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

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.core.common.MeasurementUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/4/11.
 */
public class MeasurementUtilTest {

    @Test
    public void testMeasureValueProcess() {
        Map map = new HashMap();
        map.put("a", "xxx");
        map.put("abbb", "sss");
        String str = (String) MeasurementUtil.printValue(map);
        String str2 = JSON.toJSONString(map);
        System.out.println(str);
        System.out.println(str2);

        Assert.assertNotEquals(str, str2);
    }
}
