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
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class BinaryExpr implements Expr {
    ItemType       op;
    Expr           lhs;
    Expr           rhs;
    VectorMatching vectorMatching;
    boolean        returnBool;

    public BinaryExpr(ItemType op, Expr lhs, Expr rhs, VectorMatching vectorMatching,
                      boolean returnBool) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.vectorMatching = vectorMatching;
        this.returnBool = returnBool;
    }

    @Override
    public ValueType type() {
        if (lhs.type() == ValueType.scalar && rhs.type() == ValueType.scalar) {
            return ValueType.scalar;
        }
        return ValueType.vector;
    }

    public ItemType getOp() {
        return op;
    }

    public Expr getLhs() {
        return lhs;
    }

    public Expr getRhs() {
        return rhs;
    }

    public VectorMatching getVectorMatching() {
        return vectorMatching;
    }

    public void setVectorMatching(VectorMatching vectorMatching) {
        this.vectorMatching = vectorMatching;
    }

    public boolean isReturnBool() {
        return returnBool;
    }

    @Override
    public void expr() {
    }
}
