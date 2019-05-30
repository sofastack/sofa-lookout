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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.common;

import org.apache.commons.lang3.StringUtils;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public final class MetricUtils {
    private MetricUtils() {
    }

    public static String formatMetricTagKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        if (key.equals("_bucket")) {
            return key;
        }
        StringBuilder sb = null;
        if (!Character.isLetter(key.charAt(0))) {
            sb = new StringBuilder();
            sb.append("m_");
        }
        int last = 0;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!isTagKeyChar(c)) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(key, last, i).append('_');
                last = i + 1;
            }
        }
        if (sb != null) {
            if (last < key.length()) {
                sb.append(key, last, key.length());
            }
            return sb.toString();
        }
        return key;
    }

    public static boolean isTagKeyChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
               || c == '_';
    }

    public static String formatMetricName(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        StringBuilder sb = null;
        if (!Character.isLetter(name.charAt(0))) {
            sb = new StringBuilder();
            sb.append("m_");
        }
        int last = 0;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!isMetricNameChar(c)) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(name, last, i).append('_');
                last = i + 1;
            }
        }
        if (sb != null) {
            if (last < name.length()) {
                sb.append(name, last, name.length());
            }
            return sb.toString();
        }
        return name;
    }

    public static boolean isMetricNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
               || c == '_' || c == ':' || c == '.';
    }

    public static String formatTagValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        StringBuilder sb = null;
        int len = 0;
        boolean valid = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r' || c == '\"' || c == '\\' || c == '='
                || c == ' ' || c == ',') {
                if (valid) {
                    valid = false;
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    if (len > 0) {
                        sb.append(value, i - len, i);
                    }
                    sb.append('_');
                    len = 0;
                }
                len++;
            } else {
                if (!valid) {
                    valid = true;
                    len = 0;
                }
            }
            len++;
        }
        if (sb != null) {
            if (valid && len > 0) {
                sb.append(value, value.length() - len, value.length());
            }
            return sb.toString();
        }
        return value;
    }

}
