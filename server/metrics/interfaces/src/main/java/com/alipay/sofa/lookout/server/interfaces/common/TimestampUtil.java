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
package com.alipay.sofa.lookout.server.interfaces.common;

/**
 * Created by kevin.luy@alipay.com on 2018/3/6.
 */
public final class TimestampUtil {

    private static final long VAV = 10000000000L; //10~11位边界值;

    private TimestampUtil() {
    }

    //13->10
    public static long mills2sec(long timestamp) {
        return timestamp < VAV ? timestamp : timestamp / 1000;
    }

    //10->13
    public static long sec2mills(String timestampStr) {
        if (timestampStr != null && timestampStr.contains(".")) {
            return (long) (Double.valueOf(timestampStr) * 1000);
        }
        long timestamp = Long.parseLong(timestampStr);
        return timestamp < VAV ? timestamp * 1000 : timestamp;
    }
}
