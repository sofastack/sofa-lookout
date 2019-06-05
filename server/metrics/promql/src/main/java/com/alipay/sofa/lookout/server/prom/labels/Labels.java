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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Labels is a sorted set of labels.
 * <p>
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Labels {

    public static final String MetricName = "__name__";

    public static final String BUCKET_TAG = "_bucket";
    public static final String BUCKET_LABEL = "le";

    private TreeSet<Label> labels = new TreeSet<>(new Comparator<Label>() {
        @Override
        public int compare(Label o1, Label o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });

    public int len() {
        return labels.size();
    }


    public void add(Label label) {
        labels.add(label);
    }

    public void remove(Label label) {
        labels.remove(label);
    }

    public void del(List<String> labelNames) {
        for (String labelName : labelNames) {
            del(labelName);
        }
    }

    public void del(String labelName) {
        Iterator<Label> it = labels.iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(labelName)) {
                it.remove();
            }
        }
    }

    public TreeSet<Label> getLabels() {
        return labels;
    }

    public String getValue(String labelName) {
        for (Label x : labels) {
            if (x.getName().equals(labelName)) {
                return x.getValue();
            }
        }
        return null;
    }

    public Labels clone() {
        Labels l = new Labels();
        l.labels.addAll(labels);
        return l;
    }

    public void set(String labelName, String labelValue) {
        labels.add(new Label(labelName, labelValue));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (labels != null && labels.size() > 0) {
            for (Label l : labels) {
                sb.append(l.getName());
                sb.append("=");
                sb.append("\"");
                sb.append(l.getValue());
                sb.append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Labels labels1 = (Labels) o;
        if (labels.size() != labels1.labels.size()) {
            return false;
        }
        for (Label label : labels) {
            if (!labels1.labels.contains(label)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (labels == null) {
            return 0;
        }
        int result = 1;
        for (Object element : labels) {
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }
}
