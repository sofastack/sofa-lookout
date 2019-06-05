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
package com.alipay.sofa.lookout.server.prom.ql.lex;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class LexerTest {

    @Test
    public void testLexInstantSelector() {

        Lexer lexer = Lexer.lex("http_requests:rate5m{method=\"get\"}");
        Assert.assertEquals("http_requests:rate5m", lexer.nextItem().val);
        Assert.assertEquals("{", lexer.nextItem().val);
        Assert.assertEquals("method", lexer.nextItem().val);
        Assert.assertEquals("=", lexer.nextItem().val);
        Assert.assertEquals("\"get\"", lexer.nextItem().val);
        Assert.assertEquals("}", lexer.nextItem().val);
        Assert.assertEquals(ItemType.itemEOF, lexer.nextItem().typ);
        Assert.assertNull(lexer.nextItem());
    }

    @Test
    public void testLexRangeSelector() {
        Lexer lexer = Lexer.lex("sum(http_requests_total[5m]) by (job)");
        String str = "";
        ItemType it = ItemType.itemEOF;
        do {
            Item item = lexer.nextItem();
            it = item.typ;
            System.out.print(item);
            str += item;
        } while (it != ItemType.itemEOF);

        Assert.assertEquals("<aggr:sum>(http_requests_total[5m])<by>(job)EOF", str);

    }
}
