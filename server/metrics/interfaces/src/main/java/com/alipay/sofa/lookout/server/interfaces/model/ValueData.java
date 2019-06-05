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

import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;

/**
 * Created by kevin.luy@alipay.com on 2018/2/26.
 */
public final class ValueData<T> {

    private String status = "success";
    private Object data;
    private Object debugInfo;

    public ValueData() {
    }

    public static ValueData newValueData(boolean ok, ValueType valueType, Object value) {
        return new ValueData(ok, valueType, value);
    }

    public ValueData(boolean ok, ValueType valueType, T value) {
        status = ok ? "success" : "failure";
        data = new Data(valueType.name(), value);
    }

    public ValueData(boolean ok, Object value) {
        status = ok ? "success" : "failure";
        data = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getDebugInfo() {
        return debugInfo;
    }

    public void setDebugInfo(Object debugInfo) {
        this.debugInfo = debugInfo;
    }

    public static class Data<T> {
        private String resultType;
        private T      result;

        public Data() {
        }

        public Data(String resultType, T result) {
            this.resultType = resultType;
            this.result = result;
        }

        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public T getResult() {
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }
    }

}
