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

/**
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class ParseErr {
    int    line;
    int    pos;
    String err;

    public ParseErr(int line, int pos, String err) {
        this.line = line;
        this.pos = pos;
        this.err = err;
    }

    public String error() {
        if (line == 0) {
            return String.format("parse error at char %d: %s", pos, err);
        }
        return String.format("parse error at line %d, char %d: %s", line, pos, err);
    }

    @Override
    public String toString() {
        return error();
    }
}
