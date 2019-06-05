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

import com.alipay.sofa.lookout.gateway.core.common.LogUtils;
import org.slf4j.Logger;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-03-12 15:36
 **/
public class DigestLogUtil {
    private static final Logger DIGEST_LOGGER = LogUtils.DIGEST_LOGGER;

    public static void print(AccessRecord r) {
        if (DIGEST_LOGGER.isInfoEnabled()) {
            DIGEST_LOGGER.info("type:{},client:{},src:{},app:{},pri:{},size:{},token:{}",
                r.getType(), r.getClientIp(), r.getSrc(), r.getClientApp(), r.getPriority(),
                r.getBodySize(), r.getToken());
        }
    }

    public static class AccessRecord {
        //trace,metrics or event
        private String type      = "?";
        private String clientIp;
        private String src       = "?";
        private String clientApp = "";
        private String priority  = "";
        private long   bodySize;
        private String token     = "";

        public String getClientIp() {
            return clientIp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public AccessRecord setClientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public String getSrc() {
            return src;
        }

        public AccessRecord setSrc(String src) {
            this.src = src;
            return this;
        }

        public String getClientApp() {
            return clientApp;
        }

        public AccessRecord setClientApp(String clientApp) {
            this.clientApp = clientApp;
            return this;
        }

        public String getPriority() {
            return priority;
        }

        public AccessRecord setPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public long getBodySize() {
            return bodySize;
        }

        public AccessRecord setBodySize(long bodySize) {
            this.bodySize = bodySize;
            return this;
        }

        public String getToken() {
            return token;
        }

        public AccessRecord setToken(String token) {
            this.token = token;
            return this;
        }

        @Override
        public String toString() {
            return "{" + "type='" + type + '\'' + ", clientIp='" + clientIp + '\'' + ", src='"
                   + src + '\'' + ", clientApp='" + clientApp + '\'' + ", priority='" + priority
                   + '\'' + ", bodySize=" + bodySize + ", token='" + token + '\'' + '}';
        }
    }

}
