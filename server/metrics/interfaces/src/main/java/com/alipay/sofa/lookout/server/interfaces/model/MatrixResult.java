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
import com.alipay.sofa.lookout.server.prom.ql.value.Matrix;

import java.util.*;

/**
 * Created by kevin.luy@alipay.com on 2018/3/6.
 */
public class MatrixResult {

    private Map<String, String> metric = new TreeMap<>();
    /**
     * timestamp-strValue
     **/
    private List<Object[]> values = new ArrayList<>();

    public Map<String, String> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, String> metric) {
        this.metric = metric;
    }

    public List<Object[]> getValues() {
        return values;
    }

    public void setValues(List<Object[]> values) {
        this.values = values;
    }

    public static List<MatrixResult> from(Matrix matrix) {
        List<MatrixResult> matrixResultList = new LinkedList<>();
        matrix.getSeriess().stream().forEach(series -> {
            MatrixResult matrixResult = new MatrixResult();
            series.getMetric().getLabels().forEach(label -> {
                matrixResult.getMetric().put(label.getName(), label.getValue());
            });
            series.getPoints().forEach(point -> {
                Object[] p = new Object[]{TimestampUtil.mills2sec(point.getT()), String.valueOf(point.getV())};
                matrixResult.getValues().add(p);
            });
            matrixResultList.add(matrixResult);
        });

        return matrixResultList;
    }
}
