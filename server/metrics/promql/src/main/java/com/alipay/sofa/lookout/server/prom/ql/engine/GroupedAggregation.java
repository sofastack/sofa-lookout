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
package com.alipay.sofa.lookout.server.prom.ql.engine;

import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector.Sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by kevin.luy@alipay.com on 2018/2/16.
 */
public class GroupedAggregation {
    Labels                labels;
    double                value;
    double                valuesSquaredSum;
    int                   groupCount;
    PriorityQueue<Sample> heap;
    PriorityQueue<Sample> reverseHeap;

    public GroupedAggregation(Labels metric, double value, double valuesSquaredSum, int groupCount) {
        this.labels = metric;
        this.value = value;
        this.valuesSquaredSum = valuesSquaredSum;
        this.groupCount = groupCount;
    }

    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValuesSquaredSum() {
        return valuesSquaredSum;
    }

    public void setValuesSquaredSum(double valuesSquaredSum) {
        this.valuesSquaredSum = valuesSquaredSum;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    public PriorityQueue<Sample> getHeap() {
        return heap;
    }

    public void setHeap(PriorityQueue<Sample> heap) {
        this.heap = heap;
    }

    public PriorityQueue<Sample> getReverseHeap() {
        return reverseHeap;
    }

    public void setReverseHeap(PriorityQueue<Sample> reverseHeap) {
        this.reverseHeap = reverseHeap;
    }

    public List<Sample> reverse(PriorityQueue<Sample> queue) {
        List<Sample> sampleList = new ArrayList<>(queue);
        Collections.reverse(new ArrayList<>(sampleList));
        return sampleList;
    }
}
