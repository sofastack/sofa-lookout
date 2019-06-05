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
 * Created by kevin.luy@alipay.com on 2018/2/16.
 */
public class StringValue implements Value {
    long   t;
    String v;

    public StringValue(long t, String v) {
        this.t = t;
        this.v = v;
    }

    public long getT() {
        return t;
    }

    public String getV() {
        return v;
    }

    @Override
    public ValueType type() {
        return ValueType.string;
    }

    @Deprecated
    public String toJsonStr() {
        return String.format("[%d,\"%s\"]", t, v);
    }

    @Override
    public String toString() {
        return "{" + "t=" + t + ", v='" + v + '\'' + '}';
    }
}
