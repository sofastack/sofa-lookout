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

import static com.alipay.sofa.lookout.server.prom.ql.lex.ItemType.*;

/**
 * item represents a token or text string returned from the scanner.
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Item {

    ItemType typ;
    // The starting position (in bytes) of the item (in the input string).
    int      pos;
    // The value of this item
    String   val;

    public Item(ItemType t, int pos, String val) {
        this.typ = t;
        this.val = val;
        this.pos = pos;
    }

    public String desc() {
        String str = ItemTypeSupport.itemTypeStr.get(typ);
        if (str != null && str.length() > 0) {
            return str;
        }
        if (typ == itemEOF) {
            return typ.desc();
        }
        return String.format("%s %s", typ.desc());
    }

    public ItemType getTyp() {
        return typ;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        if (typ == itemEOF) {
            return "EOF";
        } else if (typ == itemError)
            return val;
        else if (typ == itemIdentifier || typ == itemMetricIdentifier)
            return val;
        else if (typ.isKeyword()) {
            return String.format("<%s>", val);
        } else if (typ.isOperator()) {
            return String.format("<op:%s>", val);
        } else if (typ.isAggregator()) {
            return String.format("<aggr:%s>", val);
        } else if (val.length() > 10) {
            return val.substring(0, 10) + "...";
        }
        return val;
    }
}
