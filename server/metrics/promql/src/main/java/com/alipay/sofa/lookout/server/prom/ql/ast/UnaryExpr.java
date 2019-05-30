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

import com.alipay.sofa.lookout.server.prom.ql.lex.ItemType;
import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;

/**
 * A unary operation.
 * Currently unary operations are only supported for Scalars.
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class UnaryExpr implements Expr {
    ItemType op;
    Expr     expr;

    public UnaryExpr(ItemType op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

    public ItemType getOp() {
        return op;
    }

    public Expr getExpr() {
        return expr;
    }

    @Override
    public ValueType type() {
        return expr.type();
    }

    @Override
    public void expr() {

    }
}
