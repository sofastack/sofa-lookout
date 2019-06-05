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

import java.util.HashMap;
import java.util.Map;

import static com.alipay.sofa.lookout.server.prom.ql.lex.ItemType.*;

/**
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
class ItemTypeSupport {
    static final Map<String, ItemType> key = new HashMap<String, ItemType>() {{
        // Operators.
        put("and", itemLAND);
        put("or", itemLOR);
        put("unless", itemLUnless);

        // Aggregators.
        put("sum", itemSum);
        put("avg", itemAvg);
        put("count", itemCount);
        put("min", itemMin);
        put("max", itemMax);
        put("stddev", itemStddev);
        put("stdvar", itemStdvar);
        put("topk", itemTopK);
        put("bottomk", itemBottomK);
        put("count_values", itemCountValues);
        put("quantile", itemQuantile);

        // Keywords.
        put("alert", itemAlert);
        put("if", itemIf);
        put("for", itemFor);
        put("labels", itemLabels);
        put("annotations", itemAnnotations);
        put("offset", itemOffset);
        put("by", itemBy);
        put("without", itemWithout);
        put("on", itemOn);
        put("ignoring", itemIgnoring);
        put("group_left", itemGroupLeft);
        put("group_right", itemGroupRight);
        put("bool", itemBool);
    }};

    static final Map<ItemType, String> itemTypeStr = new HashMap<ItemType, String>() {{
        put(itemLeftParen, "(");
        put(itemRightParen, ")");
        put(itemLeftBrace, "{");
        put(itemRightBrace, "}");
        put(itemLeftBracket, "[");
        put(itemRightBracket, "]");
        put(itemComma, ",");
        put(itemAssign, "=");
        put(itemSemicolon, ";");
        put(itemBlank, "_");
        put(itemTimes, "x");
        put(itemSUB, "-");
        put(itemADD, "+");
        put(itemMUL, "*");
        put(itemMOD, "%");

        put(itemDIV, "/");
        put(itemEQL, "==");
        put(itemNEQ, "!=");
        put(itemLTE, "<=");
        put(itemLSS, "<");
        put(itemGTE, ">=");
        put(itemGTR, ">");
        put(itemEQLRegex, "=~");
        put(itemLiteralOr, "=~|");
        put(itemNotLiteralOr, "!~|");
        put(itemNEQRegex, "!~");
        put(itemPOW, "^");
    }};


    static {
        // init data;
        // Add keywords to item type strings.
        key.entrySet().stream().forEach(e -> {
                    itemTypeStr.put(e.getValue(), e.getKey());
                }
        );
        // Special numbers.也声明为ItemNumber
        key.put("inf", itemNumber);
        key.put("nan", itemNumber);
    }


}
