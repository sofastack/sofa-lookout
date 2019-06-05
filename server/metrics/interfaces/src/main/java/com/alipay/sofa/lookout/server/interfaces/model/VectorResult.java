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
import com.alipay.sofa.lookout.server.prom.ql.value.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kevin.luy@alipay.com on 2018/3/6.
 */
public class VectorResult {
    private Map<String, String> metric = new TreeMap<>();
    /**
     * timestamp-strValue
     **/
    private Object[] value = new Object[2];

    public Map<String, String> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, String> metric) {
        this.metric = metric;
    }

    public Object[] getValue() {
        return value;
    }

    public void setValue(Object[] value) {
        this.value = value;
    }

    public static List<VectorResult> from(Vector vector) {
        List<VectorResult> vectorResults = new LinkedList<>();
        vector.getSamples().forEach(sample -> {
            VectorResult vectorResult = new VectorResult();
            sample.getLabels().getLabels().stream().forEach(label -> {
                vectorResult.getMetric().put(label.getName(), label.getValue());
            });
            vectorResult.getValue()[0] = TimestampUtil.mills2sec(sample.getT());
            vectorResult.getValue()[1] = String.valueOf(sample.getV());
            vectorResults.add(vectorResult);
        });
        return vectorResults;

    }
}
