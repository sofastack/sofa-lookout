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

import com.alipay.sofa.lookout.server.prom.exception.TooManyPointsException;
import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.labels.Matcher;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.storage.query.AbstractQueryStmt;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.alipay.sofa.lookout.server.prom.common.QueryConstants.MAX_DATA_POINTS;
import static com.alipay.sofa.lookout.server.prom.labels.Labels.MetricName;
import static com.alipay.sofa.lookout.server.prom.labels.MatchType.MatchEqual;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 11:23
 **/
public class ElasticSearchQueryStmt extends AbstractQueryStmt {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchQueryStmt.class);

    private JestClient          jestClient;
    private String              index;

    public ElasticSearchQueryStmt(JestClient jestClient, String index) {
        this.jestClient = jestClient;
        this.index = index;
    }

    @Override
    public Collection<Series> executeQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        for (Matcher m : matchers) {
            if (StringUtils.equals(MetricName, m.getName())) {
                Preconditions.checkState(m.getType() == MatchEqual, "metric name match should be equal type!");
                queryBuilder.addMustQueries(new QueryBuilder.StringQuery().addMetricName(m.getValue()).toString());
                continue;
            }

            switch (m.getType()) {
                case MatchEqual: {
                    queryBuilder.addMustQueries(new QueryBuilder.StringQuery().addTagCond(
                            m.getName(), m.getValue()
                    ).toString());
                    break;
                }
                case MatchRegexp: {
                    queryBuilder.addMustQueries(new QueryBuilder.RegexQuery(
                            m.getName(), m.getValue()
                    ).toString());
                    break;
                }
                case MatchNotEqual: {
                    queryBuilder.addMustNotQueries(new QueryBuilder.StringQuery().addTagCond(
                            m.getName(), m.getValue()
                    ).toString());
                    break;
                }
                case MatchNotRegexp: {
                    queryBuilder.addMustNotQueries(new QueryBuilder.RegexQuery(
                            m.getName(), m.getValue()
                    ).toString());
                    break;
                }
            }
        }
        String query = queryBuilder.build(startTime, endTime);

        Search search = new Search.Builder(query).addIndex(index).build();

        try {
            JestResult jestResult = jestClient.execute(search);

            if (!jestResult.isSucceeded()) {
                logger.error("execute query err:{}, query body:{}!", jestResult.getErrorMessage(), query);
                throw new RuntimeException(jestResult.getErrorMessage());
            }
            HashMap<Labels, Series> seriesMap = new HashMap<>();
            //pop to series set;
            long totalSize = jestResult.getJsonObject().getAsJsonObject("hits").getAsJsonPrimitive("total").getAsLong();
            if (totalSize > MAX_DATA_POINTS) {
                throw new TooManyPointsException(totalSize);
            }

            JsonArray elements = jestResult.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
            if (elements != null) {
                elements.forEach(jsonElement -> {
                    JsonObject obj = jsonElement.getAsJsonObject().getAsJsonObject("_source");
                    Labels labels = new Labels();
                    labels.add(new Label(MetricName, obj.get("id").getAsString()));
                    //add tags
                    JsonArray tagArr = obj.getAsJsonArray("tags");
                    tagArr.forEach(je -> {
                        List<String> kv = Splitter.on('=').splitToList(je.getAsString());
                        labels.add(new Label(kv.get(0), kv.get(1)));
                    });

                    String timeStr = obj.get("time").getAsString();
                    float val = obj.get("value").getAsFloat();
                    Series.Point point = new Series.Point(str2Time(timeStr), val);

                    Series s = seriesMap.get(labels);
                    if (s != null) {
                        s.add(point);
                    } else {
                        s = new Series();
                        s.setMetric(labels);
                        s.add(point);
                        seriesMap.put(labels, s);
                    }
                });
            }
            return seriesMap.values();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long str2Time(String timeStr) {
        return new DateTime(timeStr).toDate().getTime();
    }
}
