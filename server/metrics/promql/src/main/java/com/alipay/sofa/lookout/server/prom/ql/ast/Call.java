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

import com.alipay.sofa.lookout.server.prom.ql.func.Function;
import com.alipay.sofa.lookout.server.prom.ql.lex.ItemType;
import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;

import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class Call implements Expr {

    Function        func;   // The function that was called.
    Expressions     args;
    private Context context;

    public Call(Function func, Expressions args) {
        this.func = func;
        this.args = args;
    }

    public void setArgs(Expressions args) {
        this.args = args;
    }

    public Function getFunc() {
        return func;
    }

    public Expressions getArgs() {
        return args;
    }

    @Override
    public ValueType type() {
        return func.getReturnType();
    }

    public Context getContext() {
        if (context == null) {
            context = new Context();
        }
        return context;
    }

    @Override
    public void expr() {

    }

    public static final class Context extends ExprContext {
        private ItemType     aggregationType;
        private List<String> grouping;

        public ItemType getAggregationType() {
            return aggregationType;
        }

        public void setAggregationType(ItemType aggregationType) {
            this.aggregationType = aggregationType;
        }

        public List<String> getGrouping() {
            return grouping;
        }

        public void setGrouping(List<String> grouping) {
            this.grouping = grouping;
        }
    }
}
