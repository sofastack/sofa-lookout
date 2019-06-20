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


import com.alipay.sofa.lookout.server.prom.labels.Matcher;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class VectorSelector implements Expr {
    String        name;
    Duration      offset   = Duration.ZERO;
    List<Matcher> matchers;
    List<Series>  serieses = new ArrayList<>();

    private Context context;

    public VectorSelector(String name, List<Matcher> matchers) {
        this.name = name;
        this.matchers = matchers;
    }

    @Override
    public ValueType type() {
        return ValueType.vector;
    }

    public List<Matcher> getMatchers() {
        return matchers;
    }

    public void setOffset(Duration offset) {
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public Duration getOffset() {
        return offset;
    }

    public List<Series> getSeriess() {
        return serieses;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Context getContext() {
        if (context == null) {
            context = new Context(this);
        }
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void expr() {

    }

    public static class Context extends ExprContext {
        private VectorSelector selector;

        public Context(VectorSelector selector) {
            this.selector = selector;
        }

        public VectorSelector getVectorSelector() {
            return selector;
        }
    }
}
