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
package com.alipay.sofa.lookout.gateway.metrics.exporter.es;

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.gateway.core.common.DataType;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.AbstractBatchExporter;
import com.alipay.sofa.lookout.gateway.metrics.exporter.es.common.ESConsts;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.server.common.es.operation.ESDataType;
import com.alipay.sofa.lookout.server.common.es.operation.ESOperatorBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.alipay.sofa.lookout.server.common.es.operation.ESOperatorBuilder.DEFAULT_ES_METRICS_MAPPING;

/**
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class ESMetricExporter extends AbstractBatchExporter<Metric> {
    private static Logger           logger  = LoggerFactory.getLogger(ESMetricExporter.class);
    public static final ContentType JSON_ND = ContentType.create("application/x-ndjson");
    private final RestClient        client;
    private final String            actionMetaData;
    private final String            endpoint;

    public ESMetricExporter(String name, Registry registry, ESProperties esProperties) {
        super(name, registry, 100, DataType.METRIC);

        int timeout = esProperties.getTimeout();

        HttpHost httpHost = new HttpHost(esProperties.getHost(), esProperties.getPort(), "http");
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost)
                .setMaxRetryTimeoutMillis(timeout)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(timeout));
        String username = esProperties.getUsername();
        String password = esProperties.getPassword();

        // basic auth
        if (StringUtils.isNotEmpty(username)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            restClientBuilder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        client = restClientBuilder.build();

        String index = esProperties.getIndex();
        if (esProperties.getOperation().isAuto()) {
            logger.info("ElasticSearch operator is active");
            ESOperatorBuilder esOperatorBuilder = new ESOperatorBuilder(ESDataType.METRIC)
                    .httpHost(httpHost.toURI())
                    .index(esProperties.getIndex())
                    .mapping(esProperties.getType());
            esOperatorBuilder.build().initializeDatabase();
            //replace index with alias
            index = esOperatorBuilder.getAlias();
        }


        String type = esProperties.getType();
        if ("metrics".equalsIgnoreCase(index)) {
            actionMetaData = String.format("{ \"index\" : {  \"_type\" : \"%s\" } }%n", type);
        } else {
            actionMetaData = String.format("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\" } }%n", index, type);
        }
        endpoint = String.format("/%s/%s/_bulk", index, type);


    }

    @Override
    protected boolean flushList(List<Metric> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Metric m : list) {
            ESEntity e = ESConverter.toEsEntity(m);
            String json = JSON.toJSONString(e);
            sb.append(actionMetaData).append(json).append("\n");
        }
        //HttpEntity entity = new NStringEntity(bulkRequestBody.toString(), JSON_ND);
        HttpEntity entity = new StringEntity(sb.toString(), JSON_ND);
        client.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        return true;
    }

    @Override
    public boolean supports(DataType dataType) {
        return dataType == DataType.METRIC;
    }
}
