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
package com.alipay.sofa.lookout.gateway.core.common;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by kevin.luy@alipay.com on 2018/3/27.
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    public static long str2Time(String timeStr) {
        return new DateTime(timeStr).toDate().getTime();
    }

    public static String timestamp2ISODate(long timestamp) {
        return ISODateTimeFormat.dateTimeNoMillis().print(timestamp);
    }

    public static long parse(String window) {
        char c = window.charAt(window.length() - 1);
        int num = Integer.parseInt(window.substring(0, window.length() - 1));
        switch (c) {
            case 's':
                return num * 1000;
            case 'm':
                return num * 60 * 1000;
            case 'h':
                return num * 60 * 60 * 1000;
            case 'd':
                return num * 60 * 60 * 24 * 1000;
            default:
                throw new RuntimeException("error window format:" + window);
        }
    }

}
