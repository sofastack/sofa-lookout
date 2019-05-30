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
package com.alipay.sofa.lookout.server.prom.ql.engine;

/**
 * @author xiangfeng.xzc
 * @date 2018/8/20
 */
public class QueryHints {
    /**
     * 是否可以使用原生查询功能: 默认支持
     */
    private boolean useNative         = true;
    /**
     * 是否纯查询，不带聚合等计算功能
     */
    private boolean vectorSelectQuery = true;

    public boolean isUseNative() {
        return useNative;
    }

    public void setUseNative(boolean useNative) {
        this.useNative = useNative;
    }

    public boolean isVectorSelectQuery() {
        return vectorSelectQuery;
    }

    public void setVectorSelectQuery(boolean vectorSelectQuery) {
        this.vectorSelectQuery = vectorSelectQuery;
    }
}
