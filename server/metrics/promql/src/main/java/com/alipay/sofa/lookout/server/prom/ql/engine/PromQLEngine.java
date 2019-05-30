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

import com.alipay.sofa.lookout.server.prom.ql.ast.EvalStmt;
import com.alipay.sofa.lookout.server.prom.ql.ast.Expr;
import com.alipay.sofa.lookout.server.prom.ql.parse.Parser;
import com.alipay.sofa.lookout.server.prom.ql.value.*;
import com.alipay.sofa.lookout.server.prom.storage.Storage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.alipay.sofa.lookout.server.prom.ql.parse.Parser.typeString;

/**
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class PromQLEngine {
    private Storage       storage;
    private EngineOptions options;
    QueryAnalyzer         queryAnalyzer;

    public PromQLEngine(Storage storage) {
        this(storage, null);
    }

    public PromQLEngine(Storage storage, EngineOptions options) {
        this.storage = storage;
        this.options = options;
        this.queryAnalyzer = new QueryAnalyzer(storage);
    }

    public Query newInstantQuery(String qs, Instant time) {
        Expr expr = Parser.parseExpr(qs);
        Query qry = newQuery(expr, time, time, Duration.ZERO);
        qry.setQ(qs);
        return qry;
    }

    public Query newRangeQuery(String qs, Instant start, Instant end, Duration interval) {
        Expr expr = Parser.parseExpr(qs);
        if (expr.type() != ValueType.vector && expr.type() != ValueType.scalar) {
            throw new IllegalStateException(String.format(
                "invalid expression type %s for range query, must be Scalar or instant Vector",
                typeString(expr.type())));
        }
        Query qry = newQuery(expr, start, end, interval);
        qry.setQ(qs);
        return qry;
    }

    private Query newQuery(Expr expr, Instant startTime, Instant endTime, Duration interval) {
        EvalStmt stmt = new EvalStmt(expr, startTime, endTime, interval);
        return new Query(stmt, this);
    }

    public Value exec(Query query) {
        EvalStmt s = (EvalStmt) query.getStmt();
        //analyze
        Analysis analysis = queryAnalyzer.analyze(query);

        //select semi-finished data from the querier of storage
        queryAnalyzer.analyzeSelectorNode(analysis);

        // Instant evaluation
        if (s.getStart() == s.getEnd() && s.getInterval().toMillis() == 0) {
            return evaluateInstantValue(s);
        }
        // Range evaluation
        return evaluateRangeValue(s);
    }

    private Value evaluateRangeValue(EvalStmt s) {
        Map<Long, Series> seriesMap = new HashMap<Long, Series>();
        for (Instant ts = s.getStart(); !ts.isAfter(s.getEnd()); ts = ts.plus(s.getInterval())) {
            long t = ts.toEpochMilli();
            Evaluator evaluator = new Evaluator(t);
            Value val = evaluator.eval(s.getExpr());
            if (val instanceof Scalar) {
                // As the expression type does not change we can safely default to 0 as the fingerprint for Scalar expressions.
                Series ss = new Series();
                if (seriesMap.size() == 0) {
                    ss = new Series();
                    seriesMap.put(0L, ss);
                    continue;
                }
                ss.getPoints().add(new Series.Point(t, ((Scalar) val).getV()));
                seriesMap.put(0L, ss);

            } else if (val instanceof Vector) {
                for (Vector.Sample sample : ((Vector) val).getSamples()) {
                    long h = sample.getLabels().hashCode();
                    Series ss = seriesMap.get(h);
                    if (ss == null) {
                        ss = new Series(sample.getLabels());
                        seriesMap.put(h, ss);
                    }
                    sample.setT(t);
                    ss.getPoints().add(new Series.Point(sample.getT(), sample.getV()));
                    seriesMap.put(h, ss);
                }
            } else {
                throw new IllegalStateException(String.format(
                    "promql.PromQLEngine.exec: invalid expression type %s", val.type()));
            }
        }

        Matrix mat = new Matrix();
        for (Series ss : seriesMap.values()) {
            mat.add(ss);
        }

        return mat;
    }

    private Value evaluateInstantValue(EvalStmt s) {
        long start = s.getStart().toEpochMilli();
        Evaluator evaluator = new Evaluator(start);
        Value val = evaluator.eval(s.getExpr());
        // evaluation timestamp as the timestamp of the point.
        if (val instanceof Scalar) {
            ((Scalar) val).setT(start);
        } else if (val instanceof Vector) {
            ((Vector) val).getSamples().stream().forEach(sample -> {
                sample.setT(start);
            });
        }
        return val;

    }

    public Storage getStorage() {
        return storage;
    }
}