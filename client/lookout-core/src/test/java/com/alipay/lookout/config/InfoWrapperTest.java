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
package com.alipay.lookout.config;

import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.InfoWrapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/4/10.
 */
public class InfoWrapperTest {

    @Test
    public void testInfoWrapper() {
        Registry registry = new DefaultRegistry();

        Info<Map> info = new Info<Map>() {
            @Override
            public Map value() {
                Map map = new HashMap();
                map.put("name", "kk");
                map.put("tt", "yy");

                return map;
            }
        };

        InfoWrapper iw2 = new InfoWrapper<Map, Info<Map>>(registry.createId("hello"), info,
            registry.clock());
        String msg2 = iw2.measure().measurements().toString();
        System.out.println(msg2);
        Assert.assertTrue(msg2.contains("tt"));

    }
}
