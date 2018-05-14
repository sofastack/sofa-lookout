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
package com.alipay.lookout.core.common;

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.api.Measurement;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by kevin.luy@alipay.com on 2017/4/11.
 */
public final class MeasurementUtil {

    private MeasurementUtil() {
    }

    /**
     * 非数值或者String类型的对象进行，Json化处理(包括转义);
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> Object printValue(T value) {
        return value == null ? value : (value instanceof String || value instanceof Number ? value
            : StringEscapeUtils.escapeJson(JSON.toJSONString(value)));
    }

    public static boolean isEmptyMeasureName(String measureName) {
        return Measurement.EMPTY_STR == measureName;
    }
}
