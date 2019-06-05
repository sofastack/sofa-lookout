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

import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.storage.query.LabelValuesStatement;
import com.alipay.sofa.lookout.server.prom.storage.query.MetadataStatement;
import com.google.gson.JsonArray;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.lookout.server.prom.common.QueryConstants.MAX_LABEL_SIZE;
import static com.alipay.sofa.lookout.server.prom.labels.Labels.MetricName;

/**
 * query label values for the label name;
 *
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 15:15
 **/
public class ElasticSearchLabelValuesStmt implements LabelValuesStatement {
    private static final Logger logger = LoggerFactory
                                           .getLogger(ElasticSearchLabelValuesStmt.class);

    private String              labelName;
    private Label               label;
    private String              q;
    private long                size;
    private long                startTime;
    private long                endTime;

    private JestClient          jestClient;
    private String              index;

    public ElasticSearchLabelValuesStmt(JestClient jestClient, String index) {
        this.jestClient = jestClient;
        this.index = index;
    }

    @Override
    public LabelValuesStatement setLabelName(String labelName) {
        this.labelName = labelName;
        return this;
    }

    @Override
    public LabelValuesStatement setFilterLabel(Label filterLabel) {
        this.label = filterLabel;
        return this;
    }

    @Override
    public MetadataStatement setQueryContent(String queryContent) {
        this.q = queryContent;
        return this;
    }

    @Override
    public LabelValuesStatement getStartTime() {
        return this;
    }

    @Override
    public LabelValuesStatement setStartTime(long startTime) {
        this.startTime = startTime;
        return this;

    }

    @Override
    public LabelValuesStatement getEndTime() {
        return this;
    }

    @Override
    public LabelValuesStatement setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public String getLabelName() {
        return labelName;
    }

    @Override
    public Label getFilterLabel() {
        return label;
    }

    @Override
    public String getQueryContent() {
        return q;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public LabelValuesStatement setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public List<String> executeQuery() {
        //合理大小
        size = size <= 0 || size > MAX_LABEL_SIZE ? MAX_LABEL_SIZE : size;

        QueryBuilder queryBuilder = new QueryBuilder();
        if (label != null) {
            //辅助筛选的label是?
            if (MetricName.equals(label.getName())) {
                queryBuilder.addMustQueries(new QueryBuilder.StringQuery().addMetricName(label.getValue()).toString());
            } else {
                queryBuilder.addMustQueries(
                        new QueryBuilder.StringQuery().addTagCond(label.getName(), label.getValue()).toString());
            }
        }

        Search search = null;
        String query = null;
        //query for metric names;
        if (MetricName.equals(labelName)) {
            if (!StringUtils.isEmpty(q)) {
                //metricName的模糊条件过滤
                queryBuilder.addMustQueries(new QueryBuilder.StringQuery(true).addMetricName(q + "*").toString());
            }
            query = queryBuilder.buildMetricNameQuery(startTime, endTime, size);
        } else {
            //普通tag的模糊条件过滤:q
            query = queryBuilder.buildLabelQuery(labelName, q, startTime, endTime, size);
        }
        search = new Search.Builder(query).addIndex(index).build();

        try {
            JestResult jestResult = jestClient.execute(search);

            if (!jestResult.isSucceeded()) {
                logger.error("execute query err:{}, query body:{}!", jestResult.getErrorMessage(), query);
                throw new RuntimeException(jestResult.getErrorMessage());
            }
            List<String> tags = new ArrayList<>();
            //pop to series set;
            JsonArray elements = jestResult.getJsonObject().getAsJsonObject("aggregations").getAsJsonObject("tags").getAsJsonArray(
                    "buckets");
            if (elements != null) {
                elements.forEach(jsonElement -> {
                    String key = jsonElement.getAsJsonObject().getAsJsonPrimitive("key").getAsString();
                    tags.add(key.substring(key.indexOf('=') + 1));
                });
            }
            return tags;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
