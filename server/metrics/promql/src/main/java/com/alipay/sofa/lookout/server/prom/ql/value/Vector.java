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
package com.alipay.sofa.lookout.server.prom.ql.value;


import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.labels.Labels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 某一时刻(时间相同)，而Labels不同
 * Created by kevin.luy@alipay.com on 2018/2/15.
 */
public class Vector implements Value {
    private List<Sample> samples = new ArrayList<>();

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public void addSample(Sample sample) {
        samples.add(sample);
    }

    @Deprecated
    public String toJsonStr() {
        StringBuilder sb = new StringBuilder("[");
        if (!samples.isEmpty()) {
            int i = samples.size();
            for (Sample sample : samples) {
                sb.append("{\"metric\":{");

                int j = sample.getLabels().getLabels().size();
                for (Label label : sample.getLabels().getLabels()) {
                    sb.append("\"").append(label.getName()).append("\":").append("\"").append(label.getValue()).append("\"");
                    if (j > 1) {
                        sb.append(",");
                    }
                    j--;
                }

                sb.append("},").append("\"value\":[").append(sample.getT()).append(",\"")
                        .append(sample.getV()).append("\"]");
                sb.append("}");
                if (i > 1) {
                    sb.append(",");
                }
                i--;
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "{" + samples + '}';
    }

    @Override
    public ValueType type() {
        return ValueType.vector;
    }

    public static class Sample {
        private Labels labels;
        long t;
        double v;
        public static final Comparator<Sample> COMPARATOR = (o1, o2) -> o1.getV() > o2.getV() ? 1 : (o1.getV() < o2.getV() ? -1 : 0);

        public Sample(long t, double v, Labels metric) {
            this.t = t;
            this.v = v;
            this.labels = metric;
        }

        public long getT() {
            return t;
        }

        public void setT(long t) {
            this.t = t;
        }

        public double getV() {
            return v;
        }

        public void setV(double v) {
            this.v = v;
        }

        public Labels getLabels() {
            return labels;
        }

        public void setLabels(Labels labels) {
            this.labels = labels;
        }

        @Override
        public String toString() {
            return String.format("%s => %s @[%s]", labels, v, t);
        }

    }

}
