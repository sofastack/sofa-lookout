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
package com.alipay.sofa.lookout.server.prom.ql.value;

import java.util.ArrayList;
import java.util.List;

/**
 * Matrix is a slice of Seriess that implements sort.
 * Interface and has a String method. [multi-line]
 * <p>
 * Created by kevin.luy@alipay.com on 2018/2/15.
 */

public class Matrix implements Value {
    List<Series> seriess = new ArrayList<>();

    @Override
    public ValueType type() {
        return ValueType.matrix;
    }

    public void add(Series series) {
        this.seriess.add(series);
    }

    public List<Series> getSeriess() {
        return seriess;
    }

    @Deprecated
    public String toJsonStr() {
        StringBuilder sb = new StringBuilder("[");
        int i = seriess.size();
        for (Series series : seriess) {
            sb.append(series.toJsonStr());
            if (i > 1) {
                sb.append(",");
            }
            i--;
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Matrix{" +
                "seriess=" + seriess +
                '}';
    }
}
