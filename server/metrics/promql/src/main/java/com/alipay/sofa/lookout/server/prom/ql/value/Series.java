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

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Series is a stream of data points belonging to a metric. [one-line]
 * <p>
 * Created by kevin.luy@alipay.com on 2018/2/15.
 */
public class Series {
    private Labels         metric;
    private TreeSet<Point> points = new TreeSet<Point>();

    public Series() {
    }

    public Series(Labels metric) {
        this.metric = metric;
    }

    public void setMetric(Labels metric) {
        this.metric = metric;
    }

    public Labels getMetric() {
        return metric;
    }

    public NavigableSet<Point> getPoints() {
        return points;
    }

    public void add(Point... points) {
        if (points == null) {
            return;
        }
        for (Point point : points) {
            this.points.add(point);
        }
    }

    public Point getFirst() {
        if (points.isEmpty()) {
            return null;
        }
        return points.first();
    }

    /**
     * the value after targetTimestamp
     *
     * @param targetTimestamp
     * @return
     */
    public Point seekOne(long targetTimestamp) {
        Point p = points.floor(new Point(targetTimestamp, 0));
        if (p != null) {
            SortedSet<Point> subSet = points.headSet(p, true);
            Iterator<Point> it = subSet.iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        return p;
    }

    /**
     * the value after targetTimestamp
     *
     * 获取小于等于当前"targettimestamp"的点
     *
     * @param targetTimestamp
     * @return
     */
    public SortedSet<Point> seekSet(long targetTimestamp) {
        Point target = points.floor(new Point(targetTimestamp, 0));
        if (target == null) {
            return null;
        }
        SortedSet<Point> pointSet = new TreeSet<>();
        SortedSet<Point> subSet = points.headSet(target, true);
        if (subSet.size() >= 0) {
            Iterator<Point> it = subSet.iterator();
            while (it.hasNext()) {
                pointSet.add(it.next());
                it.remove();
            }
        }
        return pointSet;
    }

    public String toJsonStr() {
        if (points.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"metric\":{");
        int j = metric.getLabels().size();
        for (Label label : metric.getLabels()) {
            sb.append("\"").append(label.getName()).append("\":").append("\"")
                .append(label.getValue()).append("\"");
            if (j > 1) {
                sb.append(",");
            }
            j--;
        }
        sb.append("},\"value\":[");
        int i = points.size();
        for (Point point : points) {
            sb.append("[").append(point.getT()).append(",\"").append(point.getV()).append("\"]");
            if (i > 1) {
                sb.append(",");
            }
            i--;
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Series{" + "metric=" + metric + ", points=" + points + '}';
    }

    // Point represents a single data point for a given timestamp.  用于Series!
    public static class Point implements Comparable<Point> {
        long   t;
        double v;

        public Point(long t, double v) {
            this.t = t;
            this.v = v;
        }

        public double getV() {
            return v;
        }

        public void setV(double v) {
            this.v = v;
        }

        public long getT() {
            return t;
        }

        public void setT(long t) {
            this.t = t;
        }

        @Override
        public String toString() {
            return String.format("%s @[%s]", v, t);
        }

        @Override
        public int compareTo(Point o) {
            return Long.compare(t, o.getT());
        }
    }
}
