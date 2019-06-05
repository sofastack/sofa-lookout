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
 * TODO 我们这里的metrics 和原来的MetricData相比仅仅表示一条指标数据 而不是多条聚合在一起 TODO 需要兼容 INFO
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/13
、 */
public class Metric {
    private String              name;
    private double              value;
    private long                timestamp;
    // info非null时表示这是一个INFO指标
    private String              info;
    // TODO 需要复用tags来提高性能, 但需要考虑修改tags防止影响到其他对象的问题
    // TODO 先初始化防止NPE
    private Map<String, String> tags = new HashMap<>();
    private String              debugId;

    //private Map<String, Object> values;
    // private ESDataType dataType = ESDataType.OTHERS;

    // private Map<String, String> tags = new HashMap<>();
    // private Map<String, Object> metrics = new HashMap<>();
    // private long timestamp = -1L;
    // private ESDataType dataType = ESDataType.OTHERS;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    // public void setTags(Map<String, String> tags) {
    //     this.tags = tags;
    // }
    //
    // public Map<String, Object> getValues() {
    //     return values;
    // }

    // public void setValues(Map<String, Object> values) {
    //     this.values = values;
    // }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDebugId() {
        return debugId;
    }

    public void setDebugId(String debugId) {
        this.debugId = debugId;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", info='" + info + '\'' +
                ", tags=" + tags +
                '}';
    }
}

