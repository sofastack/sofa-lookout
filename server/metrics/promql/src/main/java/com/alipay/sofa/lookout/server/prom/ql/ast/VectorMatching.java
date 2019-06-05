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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/2/10.
 */
public class VectorMatching {
    /**
     * VectorMatchCardinality
     **/
    Card card;
    List<String> matchingLabels = new ArrayList<>();
    boolean on = false;
    List<String> include = new ArrayList<>();

    public VectorMatching(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public List<String> getMatchingLabels() {
        return matchingLabels;
    }

    public void setMatchingLabels(List<String> matchingLabels) {
        this.matchingLabels = matchingLabels;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public List<String> getInclude() {
        return include;
    }
}
