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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/3/2.
 */
class QueryBuilder {
    private static final String FORMAT = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[%s,{\"range\":{\"time\":{\"gte\":%d,\"lte\":%d,\"format\":\"epoch_millis\"}}}],\"must_not\":[%s]}}}";
    private static final String TAG_SELECT_FORMAT = "{\"size\":0,\"query\":{\"bool\":{\"must\":[%s{\"range\":{\"time\":{\"gte\":%d,\"lte\":%d,\"format\":\"epoch_millis\"}}}]}},\"aggs\":{\"tags\":{\"terms\":{\"field\":\"tags\",\"include\":\"%s=%s\",\"size\":%d,\"order\":{\"_count\":\"desc\"}}}}}";
    private static final String ID_SELECT_FORMAT = "{\"size\":0,\"query\":{\"bool\":{\"must\":[%s{\"range\":{\"time\":{\"gte\":%d,\"lte\":%d,\"format\":\"epoch_millis\"}}}]}},\"aggs\":{\"tags\":{\"terms\":{\"field\":\"id\",\"size\":%d,\"order\":{\"_count\":\"desc\"}}}}}";
    private List<String> mustQueries = new ArrayList<>();
    private List<String> mustNotQueries = new ArrayList<>();

    public String build(long startTime, long endTime) {
        String mqstr = "";
        String mnqstr = "";
        Preconditions.checkState(!mustQueries.isEmpty(), "main query is empty!");
        mqstr = Joiner.on(',').join(mustQueries);
        if (!mustNotQueries.isEmpty()) {
            mnqstr = Joiner.on(',').join(mustNotQueries);
        }
        return String.format(FORMAT, mqstr, startTime, endTime, mnqstr);
    }


    public String buildLabelQuery(String targetLabelName, String q, long startTime, long endTime, long size) {
        StringBuilder mqstr = new StringBuilder();
        mustQueries.stream().forEach(qry -> {
            mqstr.append(qry).append(',');
        });
        String queryContent = StringUtils.isEmpty(q) ? ".*" : q + ".*";
        return String.format(TAG_SELECT_FORMAT, mqstr.toString(), startTime, endTime, targetLabelName, queryContent, size);
    }

    public String buildMetricNameQuery(long startTime, long endTime, long size) {
        StringBuilder mqstr = new StringBuilder();
        mustQueries.stream().forEach(qry -> {
            mqstr.append(qry).append(',');
        });
        return String.format(ID_SELECT_FORMAT, mqstr.toString(), startTime, endTime, size);
    }

    public void addMustQueries(String mustQuery) {
        mustQueries.add(mustQuery);
    }

    public void addMustNotQueries(String mustNotQuery) {
        mustNotQueries.add(mustNotQuery);
    }

    public static class RegexQuery {
        private static final String FORMAT = "{\"regexp\":{\"tags\":\"%s=%s\"}}";
        private String tagName;
        private String valueExpr;

        public RegexQuery(String tagName, String valueExpr) {
            Preconditions.checkNotNull(tagName);
            Preconditions.checkNotNull(valueExpr);
            this.tagName = tagName;
            this.valueExpr = valueExpr;
        }

        @Override
        public String toString() {
            return String.format(FORMAT, tagName, valueExpr);
        }
    }


    static class StringQuery {
        private static final String WILDCARD_FORMAT = "{\"query_string\":{\"analyze_wildcard\":true,\"query\":\"%s\"}}";
        private static final String FORMAT = "{\"query_string\":{\"query\":\"%s\"}}";

        private List<String> valueExprs = new ArrayList<>();
        private boolean wildcard;

        public StringQuery() {
            this(false);
        }

        public StringQuery(boolean wildcard) {
            this.wildcard = wildcard;
        }

        public StringQuery addMetricName(String value) {
            String expr = "id:" + value;
            valueExprs.add(expr);
            return this;
        }

        public StringQuery addTagCond(String tagKey, String tagValExpr) {
            String expr = "tags.keyword:" + tagKey + "=" + tagValExpr;
            valueExprs.add(expr);
            return this;
        }

        @Override
        public String toString() {
            Preconditions.checkState(!valueExprs.isEmpty(), " string query no conditions!");
            String queryExpr = Joiner.on(" AND ").join(valueExprs.toArray());
            if (wildcard)
                return String.format(WILDCARD_FORMAT, queryExpr);
            return String.format(FORMAT, queryExpr);
        }
    }


}



