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

import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.ql.ast.*;
import com.alipay.sofa.lookout.server.prom.ql.lex.ItemType;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector;
import com.alipay.sofa.lookout.server.prom.ql.value.*;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector.Sample;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;

import static com.alipay.sofa.lookout.server.prom.labels.Labels.MetricName;
import static com.alipay.sofa.lookout.server.prom.ql.ast.Card.*;
import static com.alipay.sofa.lookout.server.prom.ql.lex.ItemType.*;
import static com.alipay.sofa.lookout.server.prom.ql.parse.Parser.typeString;

/**
 * Created by kevin.luy@alipay.com on 2018/2/12.
 */
public class Evaluator {
    private long                 timestamp     = -1l;                  // time in milliseconds
    private static final Object  OBJ           = new Object();
    public static final Duration LookbackDelta = Duration.ofMinutes(1);

    public Evaluator(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * This is the top-level evaluation method.
     *
     * @param expr the expression parsed from promQL.
     * @return
     */
    public Value eval(Expr expr) {
        if (expr instanceof AggregateExpr) {
            // TODO count 需要特殊处理
            AggregateExpr e = (AggregateExpr) expr;
            Vector vector = evalVector(e.getExpr());
            return aggregation(e.getOp(), e.getGrouping(), e.isWithout(), e.getParam(), vector);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr e = (BinaryExpr) expr;
            Value lhs = evalOneOf(e.getLhs(), ValueType.scalar, ValueType.vector);
            Value rhs = evalOneOf(e.getRhs(), ValueType.scalar, ValueType.vector);

            ValueType lt = lhs.type();
            ValueType rt = rhs.type();

            if (lt == ValueType.scalar && rt == ValueType.scalar) {
                double v = scalarBinop(e.getOp(), ((Scalar) lhs).getV(), ((Scalar) rhs).getV());
                long t = this.timestamp;
                return new Scalar(t, v);
            } else if (lt == ValueType.vector && rt == ValueType.vector) {
                switch (e.getOp()) {
                    case itemLAND:
                        return VectorAnd((Vector) lhs, (Vector) rhs, e.getVectorMatching());
                    case itemLOR:
                        return VectorOr((Vector) lhs, (Vector) rhs, e.getVectorMatching());
                    case itemLUnless:
                        return VectorUnless((Vector) lhs, (Vector) rhs, e.getVectorMatching());
                    default:
                        return VectorBinop(e.getOp(), (Vector) lhs, (Vector) rhs,
                            e.getVectorMatching(), e.isReturnBool());
                }
            } else if (lt == ValueType.vector && rt == ValueType.scalar) {
                return VectorscalarBinop(e.getOp(), (Vector) lhs, (Scalar) rhs, false,
                    e.isReturnBool());
            } else if (lt == ValueType.scalar && rt == ValueType.vector) {
                return VectorscalarBinop(e.getOp(), (Vector) rhs, (Scalar) lhs, true,
                    e.isReturnBool());
            }

        } else if (expr instanceof Call) {
            Call e = (Call) expr;
            return e.getFunc().getCall().invoke(this, e.getArgs());

        } else if (expr instanceof MatrixSelector) {
            return matrixSelector((MatrixSelector) expr);

        } else if (expr instanceof NumberLiteral) {
            return new Scalar(this.timestamp, ((NumberLiteral) expr).getVal());
        } else if (expr instanceof ParenExpr) {
            return eval(((ParenExpr) expr).getExpr());
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr e = (UnaryExpr) expr;
            Value se = evalOneOf(((UnaryExpr) expr).getExpr(), ValueType.scalar, ValueType.vector);
            //operators: +,-
            if (e.getOp() == itemSUB) {
                if (se instanceof Scalar) {
                    ((Scalar) se).setV(0 - ((Scalar) se).getV());
                } else if (se instanceof Vector) {
                    for (Vector.Sample sample : ((Vector) se).getSamples()) {
                        sample.setV(0 - sample.getV());
                    }
                }
            }
            return se;
        } else if (expr instanceof VectorSelector) {
            VectorSelector e = (VectorSelector) expr;
            return vectorSelector(e);
        }
        errorf("unrecognized expression: %T", expr);
        return null;
    }

    private Scalar evalScalar(Expr e) {
        Value val = eval(e);
        if (!(val instanceof Scalar)) {
            errorf("expected Scalar but got %s", typeString(val.type()));
        }
        return (Scalar) val;
    }

    private Vector evalVector(Expr e) {
        Value val = eval(e);
        if (!(val instanceof Vector)) {
            errorf("expected instant Vector but got %s", typeString(val.type()));
        }
        return (Vector) val;
    }

    private long evalInt(Expr e) {
        Scalar sc = evalScalar(e);
        return (long) sc.getV();
    }

    private double evalFloat(Expr e) {
        return evalScalar(e).getV();
    }

    public Matrix evalMatrix(Expr e) {
        Value val = eval(e);
        if (!(val instanceof Matrix)) {
            errorf("expected range Vector but got %s", typeString(val.type()));
        }
        return (Matrix) val;
    }

    private StringValue evalString(Expr e) {
        Value val = eval(e);
        return (StringValue) val;
    }

    private Value evalOneOf(Expr e, ValueType t1, ValueType t2) {
        Value val = eval(e);
        if (val.type() != t1 && val.type() != t2) {
            errorf("expected %s or %s but got %s", typeString(t1), typeString(t2),
                typeString(val.type()));
        }
        return val;
    }

    /**
     * evaluates VectorSelector
     *
     * @param node
     * @return
     */
    private Vector vectorSelector(VectorSelector node) {
        Vector vec = new Vector();
        //计算offset后得到的真实时间点；
        long refTime = timestamp - durationMilliseconds(node.getOffset());
        //offset包含在内的时间区段内的series[].
        for (Series series : node.getSeriess()) {
            //晚于refTime的最最接近的一个时刻值;
            Series.Point p = series.seekOne(refTime);
            if (p == null) {
                continue;
            }
            long t = p.getT();
            double v = p.getV();
            Vector.Sample sample = new Vector.Sample(t, v, series.getMetric());
            vec.addSample(sample);
        }
        return vec;
    }

    private static long durationMilliseconds(Duration offset) {
        return offset.toMillis();
    }

    /**
     * promethues 方法逻辑，基于某时间区间的分隔点:timestamp & offset，计算每个区间内包含的点；
     * 第一个区间：左闭合，右闭合；
     * 第二个区间开始，就左开，右闭合。 因为左边界点（如果恰好有），已经在上个区间被取用了。
     *
     * @param node
     * @return
     */
    Matrix matrixSelector(MatrixSelector node) {
        long offset = durationMilliseconds(node.getOffset());
        long maxt = timestamp - offset;
        long mint = maxt - durationMilliseconds(node.getRange());
        Matrix matrix = new Matrix();
        for (Series series : node.getSeriess()) {
            Series ss = new Series(series.getMetric());
            List<Series.Point> allPoints = new LinkedList<>();
            SortedSet<Series.Point> points = series.seekSet(maxt);
            if (points == null) {
                continue;
            }
            for (Series.Point p : points) {
                long t = p.getT();
                //确保第一区间内: min<=t<=maxt;
                if (t < mint || t > maxt) {
                    continue;
                }
                allPoints.add(p);
            }
            ss.getPoints().addAll(allPoints);
            if (ss.getPoints().size() > 0) {
                matrix.add(ss);
            }
        }
        return matrix;
    }

    private Vector VectorAnd(Vector lhs, Vector rhs, VectorMatching matching) {
        if (matching.getCard() != CardManyToMany) {
            error("set operations must only use many-to-many matching");
        }
        MetricSignCalculator sigf = metricSignatureFunc(matching.isOn(), matching.getMatchingLabels());
        Vector result = new Vector();
        Map<Long, Object> rightSigs = new HashMap<>();
        // Add all rhs samples to a map so we can easily find matches later.
        for (Vector.Sample rs : rhs.getSamples()) {
            rightSigs.put(sigf.invoke(rs.getLabels()), OBJ);
        }
        for (Vector.Sample ls : lhs.getSamples()) {
            if (rightSigs.containsKey(sigf.invoke(ls.getLabels()))) {
                result.addSample(ls);
            }
        }
        return result;
    }

    private Vector VectorOr(Vector lhs, Vector rhs, VectorMatching matching) {
        if (matching.getCard() != CardManyToMany) {
            error("set operations must only use many-to-many matching");
        }
        MetricSignCalculator sigf = metricSignatureFunc(matching.isOn(), matching.getMatchingLabels());
        Vector result = new Vector();
        Map<Long, Object> leftSigs = new HashMap<>();
        for (Sample ls : lhs.getSamples()) {
            leftSigs.put(sigf.invoke(ls.getLabels()), OBJ);
            result.addSample(ls);
        }
        for (Sample rs : rhs.getSamples()) {
            if (!leftSigs.containsKey(sigf.invoke(rs.getLabels()))) {
                result.addSample(rs);
            }
        }
        return result;
    }

    private Vector VectorUnless(Vector lhs, Vector rhs, VectorMatching matching) {
        if (matching.getCard() != CardManyToMany) {
            error("set operations must only use many-to-many matching");
        }
        MetricSignCalculator sigf = metricSignatureFunc(matching.isOn(),
            matching.getMatchingLabels());
        Map<Long, Object> rightSigs = new HashMap<Long, Object>();
        for (Sample rs : rhs.getSamples()) {
            rightSigs.put(sigf.invoke(rs.getLabels()), new Object());
        }
        Vector result = new Vector();
        for (Sample ls : lhs.getSamples()) {
            if (!rightSigs.containsKey(sigf.invoke(ls.getLabels()))) {
                result.addSample(ls);
            }
        }
        return result;
    }

    /**
     * VectorBinop evaluates a binary operation between two Vectors, excluding set operators.
     *
     * @param op
     * @param lhs
     * @param rhs
     * @param matching
     * @param returnBool
     * @return
     */
    private Vector VectorBinop(ItemType op, Vector lhs, Vector rhs, VectorMatching matching, boolean returnBool) {
        if (matching.getCard() == CardManyToMany) {
            error("many-to-many only allowed for set operators");
        }
        Vector result = new Vector();
        MetricSignCalculator sigf = metricSignatureFunc(matching.isOn(), matching.getMatchingLabels());
        if (matching.getCard() == CardOneToMany) {
            lhs = rhs;
            rhs = lhs;
        }
        Map<Long, Sample> rightSigs = new HashMap<>();
        for (Sample rs : rhs.getSamples()) {
            long sig = sigf.invoke(rs.getLabels());
            if (rightSigs.containsKey(sig)) {
                errorf("many-to-many matching not allowed: matching labels must be unique on one side");
            }
            rightSigs.put(sig, rs);
        }

        // Tracks the match-signature. For one-to-one operations the value is nil. For many-to-one
        // the value is a set of signatures to detect duplicated result elements.
        Map<Long, Map<Long, Object>> matchedSigs = new HashMap<>();

        // For all lhs samples find a respective rhs sample and perform
        // the binary operation.
        for (Sample ls : lhs.getSamples()) {
            long sig = sigf.invoke(ls.getLabels());

            Sample rs = rightSigs.get(sig); // Look for a match in the rhs Vector.
            if (!rightSigs.containsKey(sig)) {
                continue;
            }

            // Account for potentially swapped sidedness.
            double vl = ls.getV();
            double vr = rs.getV();
            if (matching.getCard() == CardOneToMany) {
                vl = vr;
                vr = vl;
            }

            BinopResult br = vectorElemBinop(op, vl, vr);
            double value = br.floatValue;
            boolean keep = br.boolValue;

            if (returnBool) {
                if (keep) {
                    value = 1.0f;
                } else {
                    value = 0.0f;
                }
            } else if (!keep) {
                continue;
            }
            Labels metric = resultMetric(ls.getLabels(), rs.getLabels(), op, matching);
            Map<Long, Object> insertedSigs = matchedSigs.get(sig);
            boolean exists = matchedSigs.containsKey(sig);
            if (matching.getCard() == CardOneToOne) {
                if (exists) {
                    errorf("multiple matches for labels: many-to-one matching must be explicit (group_left/group_right)");
                }
                matchedSigs.put(sig, null); // Set existence to true.
            } else {
                // In many-to-one matching the grouping labels have to ensure a unique metric
                // for the result Vector. Check whether those labels have already been added for
                // the same matching labels.
                long insertSig = metric.hashCode();
                Object duplicate = insertedSigs.get(insertSig);
                if (!exists) {
                    insertedSigs = new HashMap<>();
                    matchedSigs.put(sig, insertedSigs);
                } else if (duplicate != null) {
                    errorf("multiple matches for labels: grouping labels must ensure unique matches");
                }
                insertedSigs.put(insertSig, OBJ);
            }
            result.addSample(new Sample(timestamp, value, metric));
        }
        return result;
    }

    /**
     * hash labels without those in the blacklist
     *
     * @param labelSet  label set
     * @param blackList labelNames need be excluded;
     * @return hash code;
     */
    private long hashWithoutLabels(Labels labelSet, List<String> blackList) {
        Labels labels = new Labels();
        for (Label l : labelSet.getLabels()) {
            boolean matched = false;
            for (String n : blackList) {
                if (l.getName().equals(n)) {
                    matched = true;
                    break;
                }
            }
            if (l.getName().equals(MetricName)) {
                continue;
            }
            if (!matched) {
                labels.add(l);
            }
        }
        return labels.hashCode();

    }

    MetricSignCalculator metricSignatureFunc(boolean on, List<String> names) {
        if (on) {
            return new MetricSignCalculator() {
                @Override
                public long invoke(Labels labels) {
                    return hashForLabels(labels, names);
                }
            };
        }
        return new MetricSignCalculator() {
            @Override
            public long invoke(Labels labels) {
                return hashWithoutLabels(labels, names);
            }
        };
    }

    /**
     * returns the metric for the given sample(s) based on the Vector binary operation and the matching options
     *
     * @param lhs
     * @param rhs
     * @param op
     * @param matching
     * @return
     */
    private Labels resultMetric(Labels lhs, Labels rhs, ItemType op, VectorMatching matching) {
        Labels lb = lhs.clone();

        if (shouldDropMetricName(op)) {
            lb.del(MetricName);
        }

        if (matching.getCard() == CardOneToOne) {
            if (matching.isOn()) {
                //不匹配的就删了，label；
                for (Label l : lhs.getLabels()) {
                    boolean matched = false;
                    for (String n : matching.getMatchingLabels()) {
                        if (StringUtils.equals(l.getName(), n)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        lb.del(l.getName());
                    }
                }
            } else {
                lb.del(matching.getMatchingLabels());
            }
        }
        for (String ln : matching.getInclude()) {
            // Included labels from the `group_x` modifier are taken from the "one"-side.
            String v = rhs.getValue(ln);
            if (StringUtils.isNoneEmpty(v)) {
                lb.set(ln, v);
            } else {
                lb.del(ln);
            }
        }

        return lb;
    }

    /**
     * @param op         operator
     * @param lhs        left vector
     * @param rhs        right vector
     * @param swap       swap lhs and rhs.
     * @param returnBool
     * @return
     */
    private Vector VectorscalarBinop(ItemType op, Vector lhs, Scalar rhs, boolean swap,
                                     boolean returnBool) {
        Vector vec = new Vector();
        for (Sample lhsSample : lhs.getSamples()) {
            double lv = lhsSample.getV();
            double rv = rhs.getV();
            if (swap) {
                lv = rv;
                rv = lv;
            }
            BinopResult br = vectorElemBinop(op, lv, rv);
            double value = br.floatValue;
            boolean keep = br.boolValue;
            if (returnBool) {
                if (keep) {
                    value = 1.0f;
                } else {
                    value = 0.0f;
                }
                keep = true;
            }
            if (keep) {
                lhsSample.setV(value);
                if (shouldDropMetricName(op)) {
                    lhsSample.setLabels(dropMetricName(lhsSample.getLabels()));
                }
                vec.addSample(lhsSample);
            }
        }
        return vec;
    }

    private boolean shouldDropMetricName(ItemType op) {
        switch (op) {
            case itemADD:
            case itemSUB:
            case itemDIV:
            case itemMUL:
            case itemMOD:
                return true;
            default:
                return false;
        }
    }

    /**
     * remove metric name label from lables
     *
     * @param label
     * @return labels
     */
    public Labels dropMetricName(Labels label) {
        label.del(MetricName);
        return label;
    }

    /**
     * a binary operation between two Scalars.
     *
     * @param op
     * @param lhs
     * @param rhs
     * @return
     */
    private double scalarBinop(ItemType op, double lhs, double rhs) {
        switch (op) {
            case itemADD:
                return lhs + rhs;
            case itemSUB:
                return lhs - rhs;
            case itemMUL:
                return lhs * rhs;
            case itemDIV:
                return lhs / rhs;
            case itemPOW:
                return (double) Math.pow((double) lhs, (double) (rhs));
            case itemMOD:
                return lhs % rhs;
            case itemEQL:
                return btos(lhs == rhs);
            case itemNEQ:
                return btos(lhs != rhs);
            case itemGTR:
                return btos(lhs > rhs);
            case itemLSS:
                return btos(lhs < rhs);
            case itemGTE:
                return btos(lhs >= rhs);
            case itemLTE:
                return btos(lhs <= rhs);
        }
        throw new IllegalStateException(String.format(
            "operator %s not allowed for Scalar operations", op));
    }

    /**
     * a binary operation between two Vector elements.
     *
     * @param op
     * @param lhs
     * @param rhs
     * @return
     */
    private BinopResult vectorElemBinop(ItemType op, double lhs, double rhs) {
        switch (op) {
            case itemADD:
                return new BinopResult(lhs + rhs, true);
            case itemSUB:
                return new BinopResult(lhs - rhs, true);
            case itemMUL:
                return new BinopResult(lhs * rhs, true);
            case itemDIV:
                return new BinopResult(lhs / rhs, true);
            case itemPOW:
                return new BinopResult(
                    Double.doubleToLongBits(Math.pow((double) lhs, (double) rhs)), true);
            case itemMOD:
                return new BinopResult(lhs % rhs, true);
            case itemEQL:
                return new BinopResult(lhs, lhs == rhs);
            case itemNEQ:
                return new BinopResult(lhs, lhs != rhs);
            case itemGTR:
                return new BinopResult(lhs, lhs > rhs);
            case itemLSS:
                return new BinopResult(lhs, lhs < rhs);
            case itemGTE:
                return new BinopResult(lhs, lhs >= rhs);
            case itemLTE:
                return new BinopResult(lhs, lhs <= rhs);
        }
        throw new IllegalStateException(String.format(
            "operator %s not allowed for operations between Vectors", op));
    }

    private class BinopResult {
        private double  floatValue;
        private boolean boolValue;

        public BinopResult(double floatValue, boolean boolValue) {
            this.floatValue = floatValue;
            this.boolValue = boolValue;
        }
    }

    /**
     * aggregation evaluates an aggregation operation on a Vector.
     *
     * @param op
     * @param grouping
     * @param without
     * @param param
     * @param vec
     * @return
     */
    private Vector aggregation(ItemType op, List<String> grouping, boolean without, Expr param, Vector vec) {
        Map<Long, GroupedAggregation> result = new HashMap<>();
        long k = 0;
        if (op == itemTopK || op == itemBottomK) {
            k = evalInt(param);
            if (k < 1) {
                return new Vector();
            }
        }
        // double q;
        if (op == itemQuantile) {
            //q = evalFloat(param);
            evalFloat(param);
        }
        String valueLabel = null;
        if (op == itemCountValues) {
            valueLabel = evalString(param).getV();
            if (!without) {
                grouping.add(valueLabel);
            }
        }

        for (Sample s : vec.getSamples()) {
            if (without) {
                s.getLabels().del(grouping);
                s.getLabels().del(MetricName);
            }
            if (op == itemCountValues) {
                s.getLabels().add(new Label(valueLabel, String.valueOf(s.getV())));
            }

            long groupingKey;
            Labels metric = s.getLabels();
            if (without) {
                groupingKey = metric.hashCode();
            } else {
                groupingKey = hashForLabels(metric, grouping);
            }

            GroupedAggregation group = result.get(groupingKey);
            // Add a new group if it doesn't exist.
            if (!result.containsKey(groupingKey)) {
                Labels m;
                if (without) {
                    m = metric;
                } else {
                    m = new Labels();
                    if (metric != null) {
                        for (Label l : metric.getLabels()) {
                            for (String n : grouping) {
                                if (l.getName().equals(n)) {
                                    m.add(new Label(n, l.getValue()));
                                    break;
                                }
                            }
                        }
                    }
                }
                result.put(groupingKey, new GroupedAggregation(m, s.getV(), s.getV() * s.getV(), 1));
                long inputVecLen = vec.getSamples().size();
                long resultSize = k;
                if (k > inputVecLen) {
                    resultSize = inputVecLen;
                }
                if (op == itemTopK || op == itemQuantile) {
                    PriorityQueue<Sample> heap = new PriorityQueue<>((int) resultSize, Sample.COMPARATOR);
                    heap.offer(s);
                    result.get(groupingKey).setHeap(heap);
                } else if (op == itemBottomK) {
                    PriorityQueue<Sample> reverseHeap = new PriorityQueue<>((int) resultSize, Sample.COMPARATOR);
                    reverseHeap.offer(s);
                    result.get(groupingKey).setReverseHeap(reverseHeap);
                }
                continue;
            }
            //如果已经存在了该分组的记录,那么进行组内聚合.(如果 native query 也不会同时也不该走到)
            switch (op) {
                case itemSum:
                    group.setValue(group.getValue() + s.getV());
                    break;

                case itemAvg:
                    group.setValue(group.getValue() + s.getV());
                    group.groupCount++;
                    break;

                case itemMax:
                    if (group.value < s.getV()) {
                        group.value = s.getV();
                    }
                    break;

                case itemMin:
                    if (group.value > s.getV()) {
                        group.value = s.getV();
                    }
                    break;

                case itemCount:
                case itemCountValues:
                    group.groupCount++;
                    break;
                case itemTopK:
                    if (group.getHeap().size() < k ||
                            group.getHeap().peek().getV() < s.getV()) {
                        if (group.getHeap().size() == k) {
                            group.getHeap().poll();
                        }
                        group.getHeap().offer(s);
                    }
                    break;

                case itemBottomK:
                    if (group.getReverseHeap().size() < k ||
                            group.getReverseHeap().peek().getV() > s.getV()) {
                        if (group.getReverseHeap().size() == k) {
                            group.getReverseHeap().poll();
                        }
                        group.getReverseHeap().offer(s);
                    }
                    break;

                default:
                    throw new IllegalStateException(String.format("expected aggregation operator but got %s", op));
            }
        }

        // create the result Vector from the aggregated groups.
        Vector resultVector = new Vector();

        for (GroupedAggregation aggr : result.values()) {
            switch (op) {
                case itemAvg:
                    aggr.value = aggr.value / aggr.groupCount;
                    break;

                case itemCount:
                case itemCountValues:
                    aggr.value = (double) aggr.groupCount;
                    break;
                case itemTopK:
                    // The heap keeps the lowest value on top, so reverse it.
                    for (Sample s : aggr.reverse(aggr.heap)) {
                        resultVector.addSample(s);
                    }
                    continue;
                    //
                case itemBottomK:
                    // The heap keeps the lowest value on top, so reverse it.
                    for (Sample s : aggr.reverse(aggr.reverseHeap)) {
                        resultVector.addSample(s);
                    }
                    continue;
                default:
            }
            resultVector.addSample(new Sample(timestamp, aggr.getValue(), aggr.labels));
        }
        return resultVector;
    }

    /**
     * hash labels on in the whitelist.
     *
     * @param labelSet
     * @param whitelist labelNames
     * @return
     */
    private int hashForLabels(Labels labelSet, List<String> whitelist) {
        if (labelSet == null) {
            return 0;
        }
        Labels labels = new Labels();
        for (Label l : labelSet.getLabels()) {
            for (String n : whitelist) {
                if (l.getName().equals(n)) {
                    labels.add(l);
                }
            }
        }
        return labels.hashCode();
    }

    private double btos(boolean b) {
        return b ? 1 : 0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private void errorf(String format, Object... args) {
        error(String.format(format, args));
    }

    private void error(String error) {
        throw new IllegalStateException(error);
    }
}
