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
package com.alipay.sofa.lookout.server.interfaces.model;

import com.alipay.sofa.lookout.server.interfaces.common.TimestampUtil;
import com.alipay.sofa.lookout.server.prom.ql.value.Scalar;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-06-05 14:17
 **/
public class ScalarResult {

    public static Object[] from(Scalar scalar) {
        Object[] value = new Object[2];
        value[0] = TimestampUtil.mills2sec(scalar.getT());
        value[1] = String.valueOf(scalar.getV());
        return value;
    }

}