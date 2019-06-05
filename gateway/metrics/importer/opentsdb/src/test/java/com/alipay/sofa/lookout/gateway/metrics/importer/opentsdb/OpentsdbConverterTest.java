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
package com.alipay.sofa.lookout.gateway.metrics.importer.opentsdb;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-14 22:23
 **/
public class OpentsdbConverterTest {

    @Test
    public void testConvertToModel() {
        Map map = new HashMap();
        map.put("app", "demo");
        JSONObject obj = new JSONObject();
        obj.put("timestamp", 1557844146565l);
        obj.put("metric", "jvm.mem.free");
        obj.put("value", 1.2d);
        obj.put("tags", map);
        System.out.println(JSONObject.toJSONString(obj));
        Metric m = OpentsdbConverter.convertToModel(obj);
        System.out.println(m);
        Assert
            .assertEquals(
                "Metric{name='jvm.mem.free', value=1.2, timestamp=1557844146565000, info='null', tags={app=demo}}",
                m.toString());
    }
}
