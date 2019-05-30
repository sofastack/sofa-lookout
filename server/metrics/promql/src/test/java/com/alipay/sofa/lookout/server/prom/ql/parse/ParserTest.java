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
package com.alipay.sofa.lookout.server.prom.ql.parse;

import com.alipay.sofa.lookout.server.prom.ql.ast.Expr;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2018/2/11.
 */
public class ParserTest {

    @Test
    public void testParserMatrixSum() {

        Expr expr1 = Parser.parseExpr("sum(http_requests_total{method=\"get\"})");
        System.out.println(expr1.toString());

        Expr expr = Parser.parseExpr("sum_over_time(http_requests_total{method=\"get\"}[5m])");
        System.out.println(expr.toString());
    }

    @Test
    public void testParserVectorSum() {
        //不能by time；
        Expr expr = Parser
            .parseExpr("sum(sum_over_time(http_requests_total{method=\"get\"}[5m])) by (job)");
        System.out.println(expr.toString());

    }

    @Test(expected = IllegalStateException.class)
    public void testParserSumOperatorWithRangeVector() {
        //sum操作符，只支持instantVector，不能出现 range相关的操作。 即时刻！！！所以IllegalStateException
        Parser.parseExpr("sum(http_requests_total{method=\"get\"}[5m]) by (job)");
    }

}
