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
package com.alipay.sofa.lookout.server.prom.ql.func;

import com.alipay.sofa.lookout.server.prom.TestBase;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.ql.ast.Expressions;
import com.alipay.sofa.lookout.server.prom.ql.ast.MatrixSelector;
import com.alipay.sofa.lookout.server.prom.ql.engine.Evaluator;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.ql.value.Value;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.List;

/**
 *
 * @author:yuanxuan
 *
 */
public class FunctionTest extends TestBase {

    @Test
    public void funcSum2OverTime() {
        Function sumOverTime = Function.getFunction("sum2_over_time");
        List<Series> series = loadMockSeries("testdata/sum_over_time.test");
        Evaluator evaluator = new Evaluator(series.get(0).getPoints().last().getT());
        Expressions expressions = new Expressions();
        MatrixSelector matrixSelector = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(60));
        matrixSelector.getSeriess().addAll(series);
        expressions.add(matrixSelector);
        Value value = sumOverTime.getCall().invoke(evaluator, expressions);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(18, vector.getSamples().get(0).getV(), 0);
        String metricName = vector.getSamples().get(0).getLabels().getValue(Labels.MetricName);
        Assert.assertEquals("test_metric", metricName);

        //test range=[90s]
        series = loadMockSeries("testdata/sum_over_time.test");
        MatrixSelector matrixSelector2 = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(90));
        matrixSelector2.getSeriess().addAll(series);
        expressions.getExpressions().clear();
        expressions.getExpressions().add(matrixSelector2);
        value = sumOverTime.getCall().invoke(evaluator, expressions);
        vector = (Vector) value;
        Assert.assertEquals(27, vector.getSamples().get(0).getV(), 0);
    }

    @Test
    public void funcSumOverTime() {
        Function sumOverTime = Function.getFunction("sum_over_time");
        List<Series> series = loadMockSeries("testdata/sum_over_time.test");
        Evaluator evaluator = new Evaluator(series.get(0).getPoints().last().getT());
        Expressions expressions = new Expressions();
        MatrixSelector matrixSelector = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(60));
        matrixSelector.getSeriess().addAll(series);
        expressions.add(matrixSelector);
        Value value = sumOverTime.getCall().invoke(evaluator, expressions);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(18, vector.getSamples().get(0).getV(), 0);
        String metricName = vector.getSamples().get(0).getLabels().getValue(Labels.MetricName);
        Assert.assertNull(metricName);

        //test range=[90s]
        series = loadMockSeries("testdata/sum_over_time.test");
        MatrixSelector matrixSelector2 = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(90));
        matrixSelector2.getSeriess().addAll(series);
        expressions.getExpressions().clear();
        expressions.getExpressions().add(matrixSelector2);
        value = sumOverTime.getCall().invoke(evaluator, expressions);
        vector = (Vector) value;
        Assert.assertEquals(27, vector.getSamples().get(0).getV(), 0);

    }

    @Test
    public void funcMaxOverTime() {
        Function sumOverTime = Function.getFunction("max_over_time");
        List<Series> series = loadMockSeries("testdata/max_over_time.test");
        Evaluator evaluator = new Evaluator(series.get(0).getPoints().last().getT());
        Expressions expressions = new Expressions();
        MatrixSelector matrixSelector = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(60));
        matrixSelector.getSeriess().addAll(series);
        expressions.add(matrixSelector);
        Value value = sumOverTime.getCall().invoke(evaluator, expressions);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(9, vector.getSamples().get(0).getV(), 0);

        //test range=[90s]
        series = loadMockSeries("testdata/max_over_time.test");
        MatrixSelector matrixSelector2 = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(90));
        matrixSelector2.getSeriess().addAll(series);
        expressions.getExpressions().clear();
        expressions.getExpressions().add(matrixSelector2);
        value = sumOverTime.getCall().invoke(evaluator, expressions);
        vector = (Vector) value;
        Assert.assertEquals(18, vector.getSamples().get(0).getV(), 0);
    }

    @Test
    public void funcMinOverTime() {
        Function sumOverTime = Function.getFunction("min_over_time");
        List<Series> series = loadMockSeries("testdata/min_over_time.test");
        Evaluator evaluator = new Evaluator(series.get(0).getPoints().last().getT());
        Expressions expressions = new Expressions();
        MatrixSelector matrixSelector = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(60));
        matrixSelector.getSeriess().addAll(series);
        expressions.add(matrixSelector);
        Value value = sumOverTime.getCall().invoke(evaluator, expressions);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(2, vector.getSamples().get(0).getV(), 0);

        //test range=[90s]
        series = loadMockSeries("testdata/min_over_time.test");
        MatrixSelector matrixSelector2 = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(90));
        matrixSelector2.getSeriess().addAll(series);
        expressions.getExpressions().clear();
        expressions.getExpressions().add(matrixSelector2);
        value = sumOverTime.getCall().invoke(evaluator, expressions);
        vector = (Vector) value;
        Assert.assertEquals(1, vector.getSamples().get(0).getV(), 0);
    }

    @Test
    public void funAvgOverTime() {
        Function sumOverTime = Function.getFunction("avg_over_time");
        List<Series> series = loadMockSeries("testdata/avg_over_time.test");
        Evaluator evaluator = new Evaluator(series.get(0).getPoints().last().getT());
        Expressions expressions = new Expressions();
        MatrixSelector matrixSelector = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(60));
        matrixSelector.getSeriess().addAll(series);
        expressions.add(matrixSelector);
        Value value = sumOverTime.getCall().invoke(evaluator, expressions);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(8.5, vector.getSamples().get(0).getV(), 0);
    }

    @Test
    public void funCountOverTime() {
        Function sumOverTime = Function.getFunction("count_over_time");
        List<Series> series = loadMockSeries("testdata/avg_over_time.test");
        Evaluator evaluator = new Evaluator(series.get(0).getPoints().last().getT());
        Expressions expressions = new Expressions();
        MatrixSelector matrixSelector = new MatrixSelector("test_metric", null,
            Duration.ofSeconds(90));
        matrixSelector.getSeriess().addAll(series);
        expressions.add(matrixSelector);
        Value value = sumOverTime.getCall().invoke(evaluator, expressions);
        Assert.assertTrue(value instanceof Vector);
        Vector vector = (Vector) value;
        Assert.assertEquals(3, vector.getSamples().get(0).getV(), 0);
    }

    @Test
    public void funcHistogramQuantile() {
    }

}