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
package com.alipay.sofa.lookout.server.storage.ext.es.operation;

import com.alipay.sofa.lookout.server.common.es.operation.ESDataType;
import com.alipay.sofa.lookout.server.common.es.operation.ESOperator;
import com.alipay.sofa.lookout.server.common.es.operation.ESOperatorBuilder;
import com.alipay.sofa.lookout.server.storage.ext.es.ElasticSearchStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-13 20:52
 **/
public class ESMetricsDataOperator {
    private static final Logger        logger = LoggerFactory
                                                  .getLogger(ESMetricsDataOperator.class);
    @Value("${metrics.server.es.index:metrics}")
    private String                     index;
    @Value("${metrics.server.es.type:metrics}")
    private String                     type;
    @Value("${metrics.server.es.operation.rollover.maxAge:1d}")
    private String                     maxAge;
    @Value("${metrics.server.es.operation.rollover.maxDocs:100000000}")
    private long                       maxDocs;
    @Value("${metrics.server.es.operation.rollover.validDays:7}")
    private int                        validDays;
    @Value("${spring.data.jest.uri:}")
    private String                     esUrl;
    private final ElasticSearchStorage storage;

    public ESMetricsDataOperator(ElasticSearchStorage storage) {
        this.storage = storage;
    }

    @PostConstruct
    public void run() {
        ESOperatorBuilder esOperatorBuilder = new ESOperatorBuilder(ESDataType.METRIC).index(index)
            .mapping(type).httpHost(esUrl).addRolloverTask(maxAge, maxDocs)
            .addDropOldIndicesTask(validDays);
        ESOperator esOperator = esOperatorBuilder.build();
        esOperator.initializeDatabase();
        esOperator.run();
        logger.info("ElasticSearch operator is active and running");
        storage.setMetricsIndex(esOperatorBuilder.getAlias());
    }
}
