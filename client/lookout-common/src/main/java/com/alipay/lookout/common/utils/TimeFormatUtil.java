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
package com.alipay.lookout.common.utils;

import com.alipay.lookout.common.log.LookoutLoggerFactory;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author wuqin
 * @version $Id: TimeFormat.java, v 0.1 2017-03-26 下午9:33 wuqin Exp $$
 */
public class TimeFormatUtil {

    private static final Logger logger = LookoutLoggerFactory.getLogger(TimeFormatUtil.class);

    public static Date convertDate(String dateStr, String format) {
        Date date;
        try {
            if (format != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                date = simpleDateFormat.parse(dateStr);
            } else {
                date = new Date(dateStr);
            }

        } catch (Exception ex) {
            logger.error("Failed to convert " + dateStr + " to Date, format:" + format);
            return null;
        }
        return date;
    }

    public static String convertStr(Date date, String format) {
        String ret;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(format);

            ret = sdf.format(date);

        } catch (Exception e) {
            logger.error("Failed to convert " + date + " to String, format:" + format);
            return null;
        }
        return ret;
    }

    public static Date getYear(String dateStr) {
        return convertDate(dateStr, "yyyy");
    }

    public static String getYear(Date date) {
        return convertStr(date, "yyyy");
    }

    public static Date getMonth(String dateStr) {
        return convertDate(dateStr, "yyyyMM");
    }

    public static String getMonth(Date date) {
        return convertStr(date, "yyyyMM");
    }

    public static Date getDay(String dateStr) {
        return convertDate(dateStr, "yyyyMMdd");
    }

    public static String getDay(Date date) {
        return convertStr(date, "yyyyMMdd");
    }

    public static Date getHour(String dateStr) {
        return convertDate(dateStr, "yyyyMMddHH");
    }

    public static String getHour(Date date) {
        return convertStr(date, "yyyyMMddHH");
    }

    public static Date getMinute(String dateStr) {
        return convertDate(dateStr, "yyyyMMddHHmm");
    }

    public static String getMinute(Date date) {
        return convertStr(date, "yyyyMMddHHmm");
    }

    public static Date getSecond(String dateStr) {
        return convertDate(dateStr, "yyyyMMddHHmmss");
    }

    public static String getSecond(Date date) {
        return convertStr(date, "yyyyMMddHHmmss");
    }
}
