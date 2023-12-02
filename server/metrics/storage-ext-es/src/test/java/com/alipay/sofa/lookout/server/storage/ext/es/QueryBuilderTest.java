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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 15:51
 **/
public class QueryBuilderTest {

    @Test
    public void testBuildQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.addMustQueries(new QueryBuilder.StringQuery().addTagCond("zone", "G*")
            .toString());
        queryBuilder.addMustQueries(new QueryBuilder.RegexQuery("tags", ".*ms").toString());
        queryBuilder.addMustNotQueries(new QueryBuilder.StringQuery().addTagCond("zone", "G*")
            .toString());
        queryBuilder.addMustNotQueries(new QueryBuilder.RegexQuery("tags", ".*ms").toString());
        String qry = queryBuilder.build(1556525226597l, 1556525226597l);
        System.out.println(qry);
        Assert
            .assertEquals(
                "{\"size\":10000,\"query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"tags.keyword:zone=G*\"}},{\"regexp\":{\"tags\":\"tags=.*ms\"}},{\"range\":{\"time\":{\"gte\":1556525226597,\"lte\":1556525226597,\"format\":\"epoch_millis\"}}}],\"must_not\":[{\"query_string\":{\"query\":\"tags.keyword:zone=G*\"}},{\"regexp\":{\"tags\":\"tags=.*ms\"}}]}}}",
                qry);
    }

    @Test
    public void testBuildQuery_no_mustnot() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.addMustQueries(new QueryBuilder.StringQuery(false).addTagCond("zone", "G*")
            .toString());
        queryBuilder.addMustQueries(new QueryBuilder.RegexQuery("tags", ".*ms").toString());
        String qry = queryBuilder.build(1556525491882l, 1556525491882l);
        System.out.println(qry);
        Assert
            .assertEquals(
                "{\"size\":10000,\"query\":{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"tags.keyword:zone=G*\"}},{\"regexp\":{\"tags\":\"tags=.*ms\"}},{\"range\":{\"time\":{\"gte\":1556525491882,\"lte\":1556525491882,\"format\":\"epoch_millis\"}}}],\"must_not\":[]}}}",
                qry);
    }

    @Test
    public void testBuildRegexQuery() {
        QueryBuilder.RegexQuery regexQuery = new QueryBuilder.RegexQuery("tags", ".*ms");
        System.out.println(regexQuery);
        Assert.assertEquals("{\"regexp\":{\"tags\":\"tags=.*ms\"}}", regexQuery.toString());

    }

    @Test
    public void testBuildStringQuery() {
        QueryBuilder.StringQuery stringQuery = new QueryBuilder.StringQuery();
        stringQuery.addTagCond("zone", "G*").addTagCond("app", "zk");
        System.out.println(stringQuery);
        Assert.assertEquals("{\"query_string\":{\"query\":\"tags.keyword:zone=G* AND tags.keyword:app=zk\"}}",
            stringQuery.toString());

        stringQuery = new QueryBuilder.StringQuery();
        stringQuery.addMetricName("jvm.count");
        System.out.println(stringQuery);
        Assert.assertEquals("{\"query_string\":{\"query\":\"id:jvm.count\"}}",
            stringQuery.toString());

    }
}
