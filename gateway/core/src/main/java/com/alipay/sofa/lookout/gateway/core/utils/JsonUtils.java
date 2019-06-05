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
package com.alipay.sofa.lookout.gateway.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author xiangfeng.xzc
 * @date 2018/12/25
 */
public final class JsonUtils {
    private JsonUtils() {
    }

    public static JSONObject warpAsJson(Map<String, Object> map) {
        if (map instanceof JSONObject) {
            return (JSONObject) map;
        }
        // 这个方法代价很小, 因为内部是直接持有map的引用的, 并没有进行深复制
        return new JSONObject(map);
    }
}
