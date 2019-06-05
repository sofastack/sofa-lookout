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

/**
 * the type of an item.
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public enum ItemType {

    itemError, itemEOF, itemComment, itemIdentifier, itemMetricIdentifier, itemLeftParen, itemRightParen, itemLeftBrace, itemRightBrace, itemLeftBracket, itemRightBracket, itemComma, itemAssign, itemSemicolon, itemString, itemNumber, itemDuration, itemBlank, itemTimes,

    operatorsStart,
    // 运算符
    itemSUB, itemADD, itemMUL, itemMOD, itemDIV, itemLAND, itemLOR, itemLUnless, itemEQL, itemNEQ, itemLTE, itemLSS, itemGTE, itemGTR, itemEQLRegex, itemNEQRegex, itemPOW, itemLiteralOr, itemNotLiteralOr, operatorsEnd,

    aggregatorsStart,
    // 聚合操作符
    itemAvg, itemCount, itemSum, itemMin, itemMax, itemStddev, itemStdvar, itemTopK, itemBottomK, itemCountValues, itemQuantile, aggregatorsEnd,

    keywordsStart,
    // 关键字
    itemAlert, itemIf, itemFor, itemLabels, itemAnnotations, itemOffset, itemBy, itemWithout, itemOn, itemIgnoring, itemGroupLeft, itemGroupRight, itemBool, keywordsEnd;

    ItemType() {
    }

    boolean isKeyword() {
        return ordinal() > keywordsStart.ordinal() && ordinal() < keywordsEnd.ordinal();
    }

    public boolean isOperator() {
        return ordinal() > operatorsStart.ordinal() && ordinal() < operatorsEnd.ordinal();
    }

    public boolean isAggregator() {
        return ordinal() > aggregatorsStart.ordinal() && ordinal() < aggregatorsEnd.ordinal();
    }

    // 带参数的聚合器
    public boolean isAggregatorWithParam() {
        return this == itemTopK || this == itemBottomK || this == itemCountValues
               || this == itemQuantile;
    }

    // 比较 操作器
    public boolean isComparisonOperator() {
        switch (this) {
            case itemEQL:
            case itemNEQ:
            case itemLTE:
            case itemLSS:
            case itemGTE:
            case itemGTR:
                return true;
            default:
                return false;
        }
    }

    // 集合 运算器
    public boolean isSetOperator() {
        switch (this) {
            case itemLAND:
            case itemLOR:
            case itemLUnless:
                return true;
        }
        return false;
    }

    static final int LowestPrec = 0;

    public int precedence() {
        switch (this) {
            case itemLOR:
                return 1;
            case itemLAND:
            case itemLUnless:
                return 2;
            case itemEQL:
            case itemNEQ:
            case itemLTE:
            case itemLSS:
            case itemGTE:
            case itemGTR:
                return 3;
            case itemADD:
            case itemSUB:
                return 4;
            case itemMUL:
            case itemDIV:
            case itemMOD:
                return 5;
            case itemPOW:
                return 6;
            default:
                return LowestPrec;
        }
    }

    public boolean isRightAssociative() {
        switch (this) {
            case itemPOW:
                return true;
            default:
                return false;
        }
    }

    //itemType description;
    public String desc() {
        switch (this) {
            case itemError:
                return "error";
            case itemEOF:
                return "end of input";
            case itemComment:
                return "comment";
            case itemIdentifier:
                return "identifier";
            case itemMetricIdentifier:
                return "metric identifier";
            case itemString:
                return "string";
            case itemNumber:
                return "number";
            case itemDuration:
                return "duration";
        }
        return this.toString();
    }

    @Override
    public String toString() {
        String str = ItemTypeSupport.itemTypeStr.get(this);
        if (str != null && str.length() > 0) {
            return str;
        }
        return String.format("<item %s>", this.name());
    }
}
