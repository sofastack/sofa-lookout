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
package com.alipay.sofa.lookout.server.storage.ext.es;

import com.alipay.sofa.lookout.server.prom.ql.lex.ItemType;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.storage.query.AggregateStatement;
import io.searchbox.client.JestClient;

import java.util.Collection;
import java.util.List;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 22:01
 **/
public class ElasticSearchAggregateStmt extends ElasticSearchQueryStmt implements
                                                                      AggregateStatement {
    private ItemType     aggregator;
    private List<String> groups;

    public ElasticSearchAggregateStmt(JestClient jestClient, String index) {
        super(jestClient, index);
    }

    @Override
    public ItemType aggregator() {
        return aggregator;
    }

    @Override
    public List<String> groups() {
        return groups;
    }

    @Override
    public AggregateStatement setAggregator(ItemType aggregator) {
        this.aggregator = aggregator;
        return this;
    }

    public AggregateStatement setGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public Collection<Series> executeQuery() {
        //return super.executeQuery();
        return null;
    }
}
