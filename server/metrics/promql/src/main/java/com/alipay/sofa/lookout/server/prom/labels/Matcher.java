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
package com.alipay.sofa.lookout.server.prom.labels;

import java.util.regex.Pattern;

/**
 * A match condition  expression in a query.
 * <p>
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Matcher {
    MatchType type;
    String    name;
    String    value;

    public Matcher(MatchType type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public boolean matches(String s) {
        switch (type) {
            case MatchEqual:
                return s == value;
            case MatchNotEqual:
                return s != value;
            case MatchRegexp:
                return matchString(s);
            case MatchNotRegexp:
                return !matchString(s);
            case MatchLiteralOr:
                return value.indexOf(s) > -1;
            case MatchNotLiteralOr:
                return !(value.indexOf(s) > -1);
        }
        throw new IllegalStateException("labels.Matcher.Matches: invalid match type");
    }

    /**
     * regex match
     *
     * @param s target
     * @return boolean
     */
    private boolean matchString(String s) {
        return Pattern.matches(value, s);
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
