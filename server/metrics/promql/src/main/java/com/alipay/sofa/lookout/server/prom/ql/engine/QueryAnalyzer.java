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

import com.alipay.sofa.lookout.server.prom.ql.ast.*;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.storage.Storage;
import com.alipay.sofa.lookout.server.prom.storage.query.AggregateStatement;
import com.alipay.sofa.lookout.server.prom.storage.query.Querier;
import com.alipay.sofa.lookout.server.prom.storage.query.QueryStatement;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static com.alipay.sofa.lookout.server.prom.common.QueryConstants.EXPRESSION_CONTEXT;
import static com.alipay.sofa.lookout.server.prom.ql.engine.Evaluator.LookbackDelta;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 18:04
 **/
public class QueryAnalyzer {
    private Storage storage;

    public QueryAnalyzer(Storage storage) {
        this.storage = storage;
    }

    public Analysis analyze(Query query) {
        //user required useNative?
        //1.如果storage不支持，那么跑错,2.如果storage支持，但用户暗示禁止，则不userNative;
        EvalStmt s = (EvalStmt) query.getStmt();
        Analysis analysis = new Analysis();
        analysis.setExpr(s.getExpr());
        //add query hint
        if (s.getExpr() instanceof VectorSelector) {
            query.hints.setVectorSelectQuery(true);
        }

        final AtomicLong maxOffset = new AtomicLong(0);
        Ast.traverses(s.getExpr(), new NodeInspector() {
            @Override
            public boolean inspect(Node node) {
                //for example,"sum(..)"
                if (node instanceof AggregateExpr) {
                    AggregateExpr agg = (AggregateExpr) node;
                    //aggr support?
                    if (!storage.querier().supportAggregator(agg.getOp())) {
                        analysis.setUseNative(false);
                        return true;
                    }

                    Expr expr = agg.getExpr();
                    if (expr instanceof Call) {
                        Call call = (Call) expr;
                        Call.Context ctx = call.getContext();
                        ctx.setAggregationType(agg.getOp());
                        ctx.setGrouping(agg.getGrouping());
                        analysis.setUseNative(true);
                    } else if (expr instanceof VectorSelector) {
                        VectorSelector vectorSelector = (VectorSelector) expr;
                        VectorSelector.Context ctx = vectorSelector.getContext();
                        ctx.setAggregationType(agg.getOp());
                        ctx.setGrouping(agg.getGrouping());
                        analysis.setUseNative(true);
                    }
                    //else expr type,useNative keep false;

                } else if (node instanceof Call) {
                    //for example,"sum_over_time(..[5m])"
                    Call call = (Call) node;
                    Call.Context callCtx = call.getContext();
                    //FIXME 需要传入[Call函数名称]
                    if (callCtx.getAggregationType() == null
                        || !storage.querier().supportAggregator(callCtx.getAggregationType())) {
                        //TODO Call function name must map with aggr;
                        analysis.setUseNative(false);
                        return true;
                    }
                    analysis.setUseNative(true);
                    for (Expr expr : call.getArgs().getExpressions()) {
                        if (expr instanceof MatrixSelector) {
                            MatrixSelector ms = (MatrixSelector) expr;
                            MatrixSelector.Context ctx = ms.getContext();
                            ctx.setAggregationType(callCtx.getAggregationType());
                            ctx.setGrouping(callCtx.getGrouping());
                            ctx.setDownsampleFunction(call.getFunc());
                        }
                    }
                } else if (node instanceof VectorSelector) {
                    VectorSelector n = (VectorSelector) node;
                    if (maxOffset.get() < LookbackDelta.toMillis()) {
                        maxOffset.set(LookbackDelta.toMillis());
                    }
                    if (n.getOffset().toMillis() + LookbackDelta.toMillis() > maxOffset.get()) {
                        maxOffset.set(n.getOffset().plus(LookbackDelta).toMillis());
                    }
                } else if (node instanceof MatrixSelector) {
                    MatrixSelector n = (MatrixSelector) node;
                    if (maxOffset.get() < n.getRange().toMillis()) {
                        maxOffset.set(n.getRange().toMillis());
                    }
                    if (n.getOffset().toMillis() + n.getRange().toMillis() > maxOffset.get()) {
                        maxOffset.set(n.getOffset().plus(n.getRange()).toMillis());
                    }
                }
                return true;
            }
        });
        Instant mint = s.getStart().minusMillis(maxOffset.get());

        analysis.setStartTime(mint.toEpochMilli());
        analysis.setEndTime(s.getEnd().toEpochMilli());
        analysis.setStep(s.getInterval().toMillis());
        //support but config disable.
        if (analysis.isUseNative() && !query.hints().isUseNative()) {
            analysis.setUseNative(false);
        }
        return analysis;
    }

    /**
     * Query semi-finished data from the storage;
     */
    public void analyzeSelectorNode(Analysis analysis) {
        Querier querier = storage.querier();
        Ast.traverses(analysis.getExpr(), new NodeInspector() {
            @Override
            public boolean inspect(Node node) {
                if (node instanceof VectorSelector) {
                    VectorSelector n = (VectorSelector) node;
                    Collection<Series> set;
                    if (analysis.isUseNative()) {
                        AggregateStatement aggrStmt = querier.createAggregateStmt();
                        aggrStmt.setStartTime(analysis.getStartTime())
                            .setEndTime(analysis.getEndTime()).setMatchers(n.getMatchers());

                        aggrStmt.setAggregator(n.getContext().getAggregationType()).setGroups(
                            n.getContext().getGrouping());
                        aggrStmt.context().put(EXPRESSION_CONTEXT, n.getContext());
                        set = aggrStmt.executeQuery();
                    } else {
                        QueryStatement queryStmt = querier.createQueryStmt();
                        queryStmt.setStartTime(analysis.getStartTime())
                            .setEndTime(analysis.getEndTime()).setMatchers(n.getMatchers());
                        queryStmt.context().put(EXPRESSION_CONTEXT, n.getContext());
                        set = queryStmt.executeQuery();
                    }

                    if (set == null) {
                        throw new RuntimeException("error selecting series set");
                    }
                    n.getSeriess().addAll(set);
                } else if (node instanceof MatrixSelector) {
                    MatrixSelector n = (MatrixSelector) node;
                    Collection<Series> set;
                    if (analysis.isUseNative()) {
                        AggregateStatement aggrStmt = querier.createAggregateStmt();
                        aggrStmt.setStartTime(analysis.getStartTime())
                            .setEndTime(analysis.getEndTime()).setMatchers(n.getMatchers());

                        aggrStmt.setAggregator(n.getContext().getAggregationType()).setGroups(
                            n.getContext().getGrouping());
                        aggrStmt.context().put(EXPRESSION_CONTEXT, n.getContext());
                        set = aggrStmt.executeQuery();
                    } else {
                        QueryStatement queryStmt = querier.createQueryStmt();
                        queryStmt.setStartTime(analysis.getStartTime())
                            .setEndTime(analysis.getEndTime()).setMatchers(n.getMatchers());
                        queryStmt.context().put(EXPRESSION_CONTEXT, n.getContext());
                        set = queryStmt.executeQuery();
                    }

                    n.getSeriess().addAll(set);
                }
                return true;
            }
        });
    }
}
