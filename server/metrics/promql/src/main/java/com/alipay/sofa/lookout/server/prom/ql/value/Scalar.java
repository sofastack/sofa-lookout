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

/**
 * Created by kevin.luy@alipay.com on 2018/2/15.
 */
public class Scalar implements Value {
    long   t;
    double v;

    public Scalar(long t, double v) {
        this.t = t;
        this.v = v;
    }

    @Override
    public ValueType type() {
        return ValueType.scalar;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    @Deprecated
    public String toJsonStr() {
        return String.format("[%d,\"%f\"]", t, v);
    }

    @Override
    public String toString() {
        return String.format("scalar: %d @[%f]", t, v);

    }

}
