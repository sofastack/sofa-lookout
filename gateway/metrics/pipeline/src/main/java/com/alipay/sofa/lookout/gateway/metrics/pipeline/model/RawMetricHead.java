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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.model;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/23
 */
public class RawMetricHead {
    /**
     * body是否被snappy压缩, standard会用到这个字段
     */
    private boolean snappy;

    /**
     * 上报的token, 非标准importer将会进行鉴权
     */
    private String  token;

    /**
     * 上报该数据的客户端ip, TODO webflux暴露的api竟然无法拿到ip地址... standard上报方式会将自己的ip地址放到头里 所以可行
     */
    private String  clientIp;

    /**
     * debug用的trace-id
     */
    private String  debugId;

    /**
     * 标准importer上报时的app字段
     */
    private String  standardAppName;

    /**
     * 标准importer上报时的优先级字段
     */
    private String  standardPriority;

    public boolean isSnappy() {
        return snappy;
    }

    public void setSnappy(boolean snappy) {
        this.snappy = snappy;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDebugId() {
        return debugId;
    }

    public void setDebugId(String debugId) {
        this.debugId = debugId;
    }

    public String getStandardAppName() {
        return standardAppName;
    }

    public void setStandardAppName(String standardAppName) {
        this.standardAppName = standardAppName;
    }

    public String getStandardPriority() {
        return standardPriority;
    }

    public void setStandardPriority(String standardPriority) {
        this.standardPriority = standardPriority;
    }

    @Override
    public String toString() {
        return "RawMetricHead{" + "snappy=" + snappy + ", token='" + token + '\'' + ", clientIp='"
               + clientIp + '\'' + ", debugId='" + debugId + '\'' + ", standardAppName='"
               + standardAppName + '\'' + ", standardPriority='" + standardPriority + '\'' + '}';
    }
}
