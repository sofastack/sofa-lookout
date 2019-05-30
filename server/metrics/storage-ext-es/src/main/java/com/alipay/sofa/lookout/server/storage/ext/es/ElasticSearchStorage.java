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
import com.alipay.sofa.lookout.server.prom.storage.Storage;
import com.alipay.sofa.lookout.server.prom.storage.query.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Cat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 10:37
 **/
public class ElasticSearchStorage implements Storage {

    private static final Logger logger      = LoggerFactory.getLogger(ElasticSearchStorage.class);
    private final JestClient    jestClient;
    private String              index;
    private boolean             healthCheck = false;
    private final long          startTime   = System.currentTimeMillis();

    private final Cat           catAlias    = new Cat.AliasesBuilder().build();

    private final Querier       querier;

    public ElasticSearchStorage(JestClient jestClient, String index) {
        this.jestClient = jestClient;
        this.index = index;
        //create es querier
        this.querier = new Querier() {
            @Override
            public boolean supportAggregator(ItemType aggregationType) {
                return false;
            }

            @Override
            public AggregateStatement createAggregateStmt() {
                return new ElasticSearchAggregateStmt(jestClient, getMetricsIndex());
            }

            @Override
            public QueryStatement createQueryStmt() {
                return new ElasticSearchQueryStmt(jestClient, getMetricsIndex());
            }

            @Override
            public MetadataStatement createLabelNamesStmt() {
                return new ElasticSearchLabelNamesStmt();
            }

            @Override
            public LabelValuesStatement createLabelValuesStmt() {
                return new ElasticSearchLabelValuesStmt(jestClient, getMetricsIndex());
            }
        };
    }

    public String getMetricsIndex() {
        return index;
    }

    public void setMetricsIndex(String index) {
        this.index = index;
    }

    @Override
    public Querier querier() {
        return querier;
    }

    @Override
    public boolean isAggregatable(String aggregator) {
        return false;
    }

    @Override
    public boolean isHealthy() {
        if (!healthCheck) {
            return true; //zsearch mode;
        }
        try {
            JestResult jestResult = jestClient.execute(catAlias);
            if (jestResult.isSucceeded()) {
                return jestResult.getJsonString().contains("active-metrics");
            }
        } catch (IOException e) {
            logger.error("check lookout health err!", e);
        }
        return false;
    }

    public void setHealthCheck(boolean healthCheck) {
        this.healthCheck = healthCheck;
    }

    @PostConstruct
    public void start() {
        logger.info("use ElasticSearch as the metrics storage");
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public void close() {

    }
}
