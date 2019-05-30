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

import com.alipay.sofa.lookout.server.prom.TestBase;
import com.alipay.sofa.lookout.server.prom.ql.ast.EvalStmt;
import com.alipay.sofa.lookout.server.prom.ql.ast.Statement;
import com.alipay.sofa.lookout.server.prom.ql.value.Matrix;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.ql.value.Value;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector;
import com.alipay.sofa.lookout.server.prom.storage.Storage;
import com.alipay.sofa.lookout.server.prom.storage.query.Querier;
import com.alipay.sofa.lookout.server.prom.storage.query.QueryStatement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 *
 * @author:yuanxuan
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PromQLEngineTest extends TestBase {

    private PromQLEngine   engine;
    @Mock
    private Storage        storage;
    @Mock
    private Querier        querier;
    @Mock
    private QueryStatement queryStatement;

    @Before
    public void setUp() throws Exception {
        engine = new PromQLEngine(storage);
    }

    @Test
    public void newInstantQuery() {
        String promql = "sum_over_time(jvm.gc.young.count{app=\"lookout-gateway\"}[60s])";
        Query query = engine.newInstantQuery(promql, Instant.parse("2019-05-29T10:29:30Z"));
        Assert.assertNotNull(query);
        String q = query.getQ();
        Assert.assertEquals(promql, q);
        Statement statement = query.getStmt();
        Assert.assertNotNull(statement);
        Assert.assertTrue(statement instanceof EvalStmt);
        EvalStmt evalStmt = (EvalStmt) statement;
        Assert.assertEquals(0, evalStmt.getInterval().getSeconds());
    }

    @Test
    public void newRangeQuery() {
        String promql = "sum_over_time(jvm.gc.young.count{app=\"lookout-gateway\"}[60s])";
        Query query = engine.newRangeQuery(promql, Instant.parse("2019-05-29T10:29:30Z"),
            Instant.parse("2019-05-29T10:32:30Z"), Duration.ofSeconds(60));
        Statement statement = query.getStmt();
        Assert.assertNotNull(statement);
        Assert.assertTrue(statement instanceof EvalStmt);
        EvalStmt evalStmt = (EvalStmt) statement;
        Assert.assertEquals(60, evalStmt.getInterval().getSeconds());

    }

    @Test
    public void test_rang_query_exec() {
        String promql = "sum_over_time(jvm.gc.young.count{app=\"lookout-gateway\"}[60s])";
        Instant start = instantWithZoned("2019-05-27T16:38:00+08:00");
        Instant end = instantWithZoned("2019-05-27T16:41:00+08:00");
        Query query = engine.newRangeQuery(promql, start, end, Duration.ofSeconds(60));
        when(storage.querier()).thenReturn(querier);
        when(querier.createQueryStmt()).thenReturn(queryStatement);
        when(queryStatement.setStartTime(anyLong())).thenReturn(queryStatement);
        when(queryStatement.setEndTime(anyLong())).thenReturn(queryStatement);
        List<Series> mockSeries = loadMockSeries("testdata/range_query.test");
        when(queryStatement.executeQuery()).thenReturn(mockSeries);
        Value value = engine.exec(query);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Matrix);
        Matrix matrix = (Matrix) value;
        Assert.assertEquals(16, matrix.getSeriess().get(0).getFirst().getV(), 0);

    }

    @Test
    public void test_query_exec() {
        String promql = "sum_over_time(jvm.gc.young.count{app=\"lookout-gateway\"}[60s])";
        Instant start = instantWithZoned("2019-05-27T16:38:00+08:00");
        Query query = engine.newInstantQuery(promql, start);
        when(storage.querier()).thenReturn(querier);
        when(querier.createQueryStmt()).thenReturn(queryStatement);
        when(queryStatement.setStartTime(anyLong())).thenReturn(queryStatement);
        when(queryStatement.setEndTime(anyLong())).thenReturn(queryStatement);
        List<Series> mockSeries = loadMockSeries("testdata/range_query.test");
        when(queryStatement.executeQuery()).thenReturn(mockSeries);
        Value value = engine.exec(query);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(16, vector.getSamples().get(0).getV(), 0);
    }
}