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
package com.alipay.sofa.lookout.server.prom.ql.ast;

import com.alipay.sofa.lookout.server.prom.util.NoopUtils;

import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class Ast {

    /**
     * traverses an AST
     *
     * @param node
     * @param f
     */
    public static void traverses(Node node, NodeInspector f) {
        Walk(f, node);
    }

    /**
     * Walk in depth-first order
     *
     * @param v    visitor of the ast tree
     * @param node tree nodes
     */
    static void Walk(Visitor v, Node node) {
        v = v.visit(node);
        if (v == null) {
            return;
        }
        if (node instanceof EvalStmt)
            Walk(v, ((EvalStmt) node).expr);
        else if (node instanceof RecordStmt) {
            Walk(v, ((RecordStmt) node).expr);
        } else if (node instanceof List) {
            for (Expr e : ((List<Expr>) node)) {
                Walk(v, e);
            }
        } else if (node instanceof Expressions) {
            Expressions n = (Expressions) node;
            for (Expr e : n.getExpressions()) {
                Walk(v, e);
            }
        } else if (node instanceof AggregateExpr) {
            Walk(v, ((AggregateExpr) node).expr);
        } else if (node instanceof BinaryExpr) {
            Walk(v, ((BinaryExpr) node).lhs);
            Walk(v, ((BinaryExpr) node).rhs);
        } else if (node instanceof Call) {
            Walk(v, ((Call) node).args);
        } else if (node instanceof ParenExpr) {
            Walk(v, ((ParenExpr) node).expr);
        } else if (node instanceof UnaryExpr) {
            Walk(v, ((UnaryExpr) node).expr);
        } else if (node instanceof MatrixSelector || node instanceof NumberLiteral
                   || node instanceof StringLiteral || node instanceof VectorSelector) {
            NoopUtils.noop();
        } else {
            throw new IllegalStateException(String.format(
                "walk through promql ast err: unhandled node type %s", node.toString()));
        }
        v.visit(null);
    }

}
