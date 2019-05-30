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

import com.alipay.sofa.lookout.server.prom.ql.ast.Statement;
import com.alipay.sofa.lookout.server.prom.ql.value.Value;

/**
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Query {
    // The original query string.
    private String       q;
    // Statement of the parsed query.
    private Statement    stmt;
    private PromQLEngine ng;
    QueryHints           hints = new QueryHints();

    public Query(Statement stmt, PromQLEngine ng) {
        this.stmt = stmt;
        this.ng = ng;
    }

    public Statement getStmt() {
        return stmt;
    }

    public PromQLEngine getNg() {
        return ng;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public QueryHints hints() {
        return hints;
    }

    public Result exec() {
        Value val = ng.exec(this);
        return new Result(val);
    }
}
