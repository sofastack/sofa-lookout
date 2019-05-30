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
package com.alipay.sofa.lookout.gateway.metrics.exporter.es;

import com.alipay.sofa.lookout.gateway.metrics.exporter.es.common.ESConsts;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/26
 */
@ConfigurationProperties(prefix = ESProperties.PREFIX)
public class ESProperties {
    public static final String PREFIX    = "metrics.exporter.es";
    private String             host      = ESConsts.DEFAULT_ES_HOST;
    private int                port      = 9200;
    private String             username  = "lookout";
    private String             password  = "dkZpMNJF";
    private int                timeout   = 5000;
    private String             index     = ESConsts.DEFAULT_ES_INDEX;
    private String             type      = ESConsts.DEFAULT_ES_TYPE;
    private Operation          operation = new Operation();

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class Operation {
        private boolean auto = true;

        public boolean isAuto() {
            return auto;
        }

        public void setAuto(boolean auto) {
            this.auto = auto;
        }

    }
}
