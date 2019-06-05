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
package com.alipay.sofa.lookout.server.prom.storage.query;

import com.alipay.sofa.lookout.server.prom.labels.Matcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 11:27
 **/
public abstract class AbstractQueryStmt implements QueryStatement {
    protected List<Matcher> matchers;
    protected long          startTime;
    protected long          endTime;
    protected Map           context = new HashMap();

    @Override
    public List<Matcher> getMatchers() {
        return matchers;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public long endTime() {
        return endTime;
    }

    @Override
    public Map context() {
        return context;
    }

    public QueryStatement setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public QueryStatement setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public QueryStatement setMatchers(List<Matcher> matchers) {
        this.matchers = matchers;
        return this;
    }
}
