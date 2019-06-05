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

import java.util.HashMap;
import java.util.Map;

/**
 * 一次原始的metric上报信息
 *
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public class RawMetric {
    /**
     * 当对 RawMetric 进行较大修改的时候, 可能导致读出来的数据不兼容. 此时我们只能舍弃旧数据, 这个版本号可以用于判断是否是新删数据
     */
    private int version;

    /**
     * 数据到达gateway的时间戳!!! prometheus会用这个时间戳作为数据的时间戳, 对于其他类型的数据这个字段可能作用不大
     */
    private long timestamp;

    /**
     * 本次上报的元信息
     */
    private RawMetricHead head = new RawMetricHead();

    /**
     * 主要是prometheus会用到这个字段 TODO 是否放入head里好一些?
     */
    private Map<String, String> extraTags = new HashMap<>();

    /**
     * 来源类型
     */
    private SourceType sourceType;

    /**
     * 原始请求体
     */
    private byte[] rawBody;

    /**
     * 是否是推模式, TODO 从 sourceType 中其实可以推断出pushMode, 后期考虑去掉
     */
    private boolean pushMode;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public RawMetricHead getHead() {
        return head;
    }

    public void setHead(RawMetricHead head) {
        this.head = head;
    }

    public Map<String, String> getExtraTags() {
        return extraTags;
    }

    public void setExtraTags(Map<String, String> extraTags) {
        this.extraTags = extraTags;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public byte[] getRawBody() {
        return rawBody;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }

    public boolean isPushMode() {
        return pushMode;
    }

    public void setPushMode(boolean pushMode) {
        this.pushMode = pushMode;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "RawMetric{" +
                "version=" + version +
                ", timestamp=" + timestamp +
                ", head=" + head +
                ", extraTags=" + extraTags +
                ", sourceType=" + sourceType +
                ", pushMode=" + pushMode +
                '}';
    }
}
