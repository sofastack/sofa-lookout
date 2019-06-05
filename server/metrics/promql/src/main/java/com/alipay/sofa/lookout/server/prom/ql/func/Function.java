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
package com.alipay.sofa.lookout.server.prom.ql.func;

import com.alipay.sofa.lookout.server.prom.exception.QLParseException;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.ql.ast.Expr;
import com.alipay.sofa.lookout.server.prom.ql.ast.Expressions;
import com.alipay.sofa.lookout.server.prom.ql.ast.MatrixSelector;
import com.alipay.sofa.lookout.server.prom.ql.engine.Evaluator;
import com.alipay.sofa.lookout.server.prom.ql.func.support.Bucket;
import com.alipay.sofa.lookout.server.prom.ql.func.support.PromBucket;
import com.alipay.sofa.lookout.server.prom.ql.func.support.Quantile;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector;
import com.alipay.sofa.lookout.server.prom.ql.value.*;

import java.time.Duration;
import java.util.*;

/**
 * Refer to prometheus's codes
 * Created by kevin.luy@alipay.com on 2018/2/12.
 */
public class Function {
    String name;
    ValueType[] argTypes;
    int variadic;
    ValueType returnType;
    FuncCallFn call;

    static Map<String, Function> functions = new HashMap<String, Function>() {{
        put("count_over_time",
                new Function("count_over_time", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcCountOverTime));
        put("avg_over_time",
                new Function("avg_over_time", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcAvgOverTime));
        put("sum_over_time",
                new Function("sum_over_time", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcSumOverTime));
        put("sum2_over_time",
                new Function("sum2_over_time", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcSum2OverTime));
        put("max_over_time",
                new Function("max_over_time", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcMaxOverTime));
        put("min_over_time",
                new Function("min_over_time", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcMinOverTime));
        put("delta", new Function("delta", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcDelta));
        put("idelta", new Function("idelta", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcIdelta));
        put("rate", new Function("rate", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcRate));
        put("increase", new Function("increase", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcIncrease));
        put("increase2", new Function("increase2", new ValueType[]{ValueType.matrix}, ValueType.vector, Function::funcIncrease2));
        put("histogram_quantile", new Function("histogram_quantile", new ValueType[]{ValueType.scalar, ValueType.vector}, ValueType.vector,
                Function::funcHistogramQuantile));

        //搭配 sofa lookout sdk 用的函数
        put("zhistogram_quantile", new Function("zhistogram_quantile", new ValueType[]{ValueType.scalar, ValueType.vector}, ValueType.vector,
                Function::funcHistogramQuantileLK));
    }};

    public Function(String name, ValueType[] argTypes, ValueType returnType, FuncCallFn call) {
        this.name = name;
        this.argTypes = argTypes;
        this.returnType = returnType;
        this.call = call;
    }

    /**
     * get function by name
     *
     * @param name func name
     * @return
     */
    public static Function getFunction(String name) {
        Function fn = functions.get(name);
        if (fn == null) {
            throw new QLParseException(String.format("No function: %s found", name));
        }
        return fn;
    }

    public ValueType[] getArgTypes() {
        return argTypes;
    }

    public int getVariadic() {
        return variadic;
    }

    public String getName() {
        return name;
    }

    public ValueType getReturnType() {
        return returnType;
    }

    public FuncCallFn getCall() {
        return call;
    }

    // === sum_over_time(Matrix ValueTypeMatrix) Vector ===
    static Value funcSum2OverTime(Evaluator ev, Expressions args) {
        return aggrOverTime(ev, args, values -> {
            float sum = 0f;
            for (Series.Point v : values) {
                sum += v.getV();
            }
            return sum;
        }, false);
    }

    static Value funcSumOverTime(Evaluator ev, Expressions args) {
        return aggrOverTime(ev, args, values -> {
            float sum = 0f;
            for (Series.Point v : values) {
                sum += v.getV();
            }
            return sum;
        });
    }

    static Value funcMaxOverTime(Evaluator ev, Expressions args) {
        return aggrOverTime(ev, args, values -> {
            double max = Double.MIN_VALUE;
            for (Series.Point v : values) {
                if (v.getV() > max) {
                    max = v.getV();
                }
            }
            return max;
        });
    }

    static Value funcMinOverTime(Evaluator ev, Expressions args) {
        return aggrOverTime(ev, args, values -> {
            double min = Float.MAX_VALUE;
            for (Series.Point v : values) {
                if (v.getV() < min) {
                    min = v.getV();
                }
            }
            return min;
        });
    }

    private static Value aggrOverTime(Evaluator ev, Expressions args, AggrFn aggrFn) {
        return aggrOverTime(ev, args, aggrFn, true);
    }

    /**
     * *_over_time, 这里参考prom标准，把区间内点聚合，算"右边界时刻"的值；
     *
     * @param ev
     * @param args
     * @param aggrFn
     * @param metricNameIgnored
     * @return
     */
    private static Value aggrOverTime(Evaluator ev, Expressions args, AggrFn aggrFn, boolean metricNameIgnored) {
        Matrix mat = ev.evalMatrix(args.getExpressions().get(0));
        Vector resultVector = new Vector();
        for (Series el : mat.getSeriess()) {
            if (el.getPoints().size() == 0) {
                continue;
            }
            Labels labels = metricNameIgnored ? ev.dropMetricName(el.getMetric()) : el.getMetric();
            resultVector.addSample(new Vector.Sample(ev.getTimestamp(), aggrFn.invoke(el.getPoints()), labels));
        }
        return resultVector;
    }

    private static Value funcDelta(Evaluator ev, Expressions args) {
        return extrapolatedRate(ev, args.getExpressions().get(0), false, false);
    }

    //不做函数拟合，插入均值，只返回一段时间内的近视值
    private static Value funcIncrease2(Evaluator ev, Expressions args) {
        MatrixSelector ms = (MatrixSelector) args.getExpressions().get(0);
        Matrix matrix = ev.evalMatrix(ms);
        Vector resultVector = new Vector();
        for (Series samples : matrix.getSeriess()) {
            // No sense in trying to compute a rate without at least two points. Drop
            // this Vector element.
            if (samples.getPoints().size() < 2) {
                continue;
            }
            double counterCorrection = 0f;
            double lastValue = 0f;
            for (Series.Point sample : samples.getPoints()) {
                //counter 发生了不是单调递增情况，衰减了(比如:  目标重启?)
                if (sample.getV() < lastValue) {
                    counterCorrection += lastValue;
                }
                lastValue = sample.getV();
            }
            //(如果重启，重启后的点需要补偿)，最后个点的值+（重启前的高点作为基数）-开始的点值；
            double resultValue = lastValue - samples.getFirst().getV() + counterCorrection;
            resultVector.addSample(new Vector.Sample(ev.getTimestamp(), resultValue, ev.dropMetricName(samples.getMetric())));
        }
        return resultVector;
    }

    private static Value extrapolatedRate(Evaluator ev, Expr arg, boolean isCounter, boolean isRate) {
        MatrixSelector ms = (MatrixSelector) arg;
        Matrix matrix = ev.evalMatrix(ms);
        long rangeStart = ev.getTimestamp() - durationMilliseconds(ms.getRange().plus(ms.getOffset()));
        long rangeEnd = ev.getTimestamp() - durationMilliseconds(ms.getOffset());
        Vector resultVector = new Vector();

        for (Series samples : matrix.getSeriess()) {
            // No sense in trying to compute a rate without at least two points. Drop
            // this Vector element.
            if (samples.getPoints().size() < 2) {
                continue;
            }

            double counterCorrection = 0f;
            double lastValue = 0f;
            Series.Point lastPoint = null;

            for (Series.Point sample : samples.getPoints()) {
                //counter 发生了不是单调递增情况，衰减了(比如:  目标重启?)
                if (isCounter && sample.getV() < lastValue) {
                    counterCorrection += lastValue;
                }
                lastValue = sample.getV();
                lastPoint = sample;
            }
            //(如果重启，重启后的点需要补偿)，最后个点的值+（重启前的高点作为基数）-开始的点值；
            double resultValue = lastValue - samples.getFirst().getV() + counterCorrection;

            // Duration between first/last samples and boundary of range. second?
            double durationToStart = (samples.getFirst().getT() - rangeStart) / 1000;
            double durationToEnd = (rangeEnd - lastPoint.getT()) / 1000;
            double sampledInterval = (lastPoint.getT() - samples.getFirst().getT()) / 1000;
            double averageDurationBetweenSamples = sampledInterval / (samples.getPoints().size() - 1);

            if (isCounter && resultValue > 0 && samples.getFirst().getV() >= 0) {
                // Counters cannot be negative. If we have any slope at
                // all (i.e. resultValue went up), we can extrapolate
                // the zero point of the counter. If the duration to the
                // zero point is shorter than the durationToStart, we
                // take the zero point as the start of the series,
                // thereby avoiding extrapolation to negative counter
                // values.
                double durationToZero = sampledInterval * (samples.getFirst().getV() / resultValue);
                if (durationToZero < durationToStart) {
                    durationToStart = durationToZero;
                }
            }

            // If the first/last samples are close to the boundaries of the range,
            // extrapolate the result. This is as we expect that another sample
            // will exist given the spacing between samples we've seen thus far,
            // with an allowance for noise.
            double extrapolationThreshold = (double) (averageDurationBetweenSamples * 1.1);
            double extrapolateToInterval = sampledInterval;

            if (durationToStart < extrapolationThreshold) {
                extrapolateToInterval += durationToStart;
            } else {
                extrapolateToInterval += averageDurationBetweenSamples / 2;
            }
            if (durationToEnd < extrapolationThreshold) {
                extrapolateToInterval += durationToEnd;
            } else {
                extrapolateToInterval += averageDurationBetweenSamples / 2;
            }
            resultValue = resultValue * (extrapolateToInterval / sampledInterval);
            if (isRate) {
                resultValue = resultValue / ms.getRange().getSeconds();
            }
            resultVector.addSample(new Vector.Sample(ev.getTimestamp(), resultValue, ev.dropMetricName(samples.getMetric())));

        }
        return resultVector;
    }

    // === idelta(node model.ValMatric) Vector ===
    private static Value funcIdelta(Evaluator ev, Expressions args) {
        return instantValue(ev, args.getExpressions().get(0), false);
    }

    private static Value instantValue(Evaluator ev, Expr arg, boolean isRate) {
        Vector resultVector = new Vector();
        Matrix matrix = ev.evalMatrix(arg);
        for (Series samples : matrix.getSeriess()) {
            // No sense in trying to compute a rate without at least two points. Drop
            // this Vector element.
            if (samples.getPoints().size() < 2) {
                continue;
            }
            Iterator<Series.Point> it = samples.getPoints().descendingIterator();
            Series.Point lastSample = it.next();
            Series.Point previousSample = it.next();

            double resultValue = 0f;
            if (isRate && lastSample.getV() < previousSample.getV()) {
                // Counter reset.
                resultValue = lastSample.getV();
            } else {
                resultValue = lastSample.getV() - previousSample.getV();
            }

            long sampledInterval = lastSample.getT() - previousSample.getT();
            if (sampledInterval == 0) {
                // Avoid dividing by 0.
                continue;
            }

            if (isRate) {
                // Convert to per-second.
                resultValue /= sampledInterval / 1000;
            }
            resultVector.addSample(new Vector.Sample(ev.getTimestamp(), resultValue, ev.dropMetricName(samples.getMetric())));
        }
        return resultVector;
    }

    // === rate(node ValueTypeMatrix) Vector ===
    private static Value funcRate(Evaluator ev, Expressions args) {
        return extrapolatedRate(ev, args.getExpressions().get(0), true, true);
    }

    // === increase(node ValueTypeMatrix) Vector ===
    private static Value funcIncrease(Evaluator ev, Expressions args) {
        return extrapolatedRate(ev, args.getExpressions().get(0), true, false);
    }


    // === avg_over_time(Matrix ValueTypeMatrix) Vector ===
    private static Value funcAvgOverTime(Evaluator ev, Expressions args) {
        return aggrOverTime(ev, args, values -> {
            double sum = 0f;
            for (Series.Point v : values) {
                sum += v.getV();
            }
            return sum / values.size();
        });
    }

    // === count_over_time(Matrix ValueTypeMatrix) Vector ===
    private static Value funcCountOverTime(Evaluator ev, Expressions args) {
        return aggrOverTime(ev, args, values -> {
            return values.size();
        });
    }

    /**
     * for prometheus
     * <p>
     * 特点：count是累积型的，每个bucket值记录upperBound
     *
     * @param ev
     * @param args
     * @return
     */
    public static Value funcHistogramQuantile(Evaluator ev, Expressions args) {
        Scalar scalar = (Scalar) ev.eval(args.getExpressions().get(0));
        double q = scalar.getV();
        Vector vector = (Vector) ev.eval(args.getExpressions().get(1));
        Map<Labels, List<PromBucket>> map = new HashMap<>();
        long time = 0;
        for (Vector.Sample sample : vector.getSamples()) {
            Labels labels = sample.getLabels();
            time = sample.getT();
            long value = (long) sample.getV();
            String bucketLabelValue = labels.getValue("le");
            if (bucketLabelValue == null) {
                continue;
            }
            double upperBound = Float.POSITIVE_INFINITY;
            if (!bucketLabelValue.equals("+Inf")) {
                upperBound = Double.parseDouble(bucketLabelValue);
            }
            PromBucket bucket = new PromBucket(upperBound, value);
            //clone
            labels = labels.clone();
            labels.del(Labels.BUCKET_LABEL);
            labels.del(Labels.MetricName);
            List<PromBucket> buckets = map.get(labels);
            if (buckets == null) {
                buckets = new ArrayList<>();
                map.put(labels, buckets);
            }
            buckets.add(bucket);
        }
        Vector result = new Vector();
        for (Map.Entry<Labels, List<PromBucket>> entry : map.entrySet()) {
            Vector.Sample sample = new Vector.Sample(time, Quantile.bucketQuantile(q, entry.getValue()), entry.getKey());
            result.addSample(sample);
        }
        return result;
    }


    /**
     * for lookout sdk!
     * <p>
     * 特点：count是窗口（累计）型，每个bucket值记录 lowerBound
     *
     * @param ev
     * @param args
     * @return
     */
    private static Value funcHistogramQuantileLK(Evaluator ev, Expressions args) {
        Scalar scalar = (Scalar) ev.eval(args.getExpressions().get(0));
        double q = scalar.getV();
        Vector vector = (Vector) ev.eval(args.getExpressions().get(1));
        Map<Labels, List<Bucket>> map = new HashMap<>();
        long time = 0;
        for (Vector.Sample sample : vector.getSamples()) {
            Labels labels = sample.getLabels();
            time = sample.getT();
            long value = (long) sample.getV();
            String bucketLabelValue = labels.getValue(Labels.BUCKET_TAG);
            if (bucketLabelValue == null) {
                continue;
            }
            Bucket bucket = Quantile.buildBucket(bucketLabelValue, value);
            //clone
            labels = labels.clone();
            labels.del(Labels.BUCKET_TAG);
            List<Bucket> buckets = map.get(labels);
            if (buckets == null) {
                buckets = new ArrayList<>();
                map.put(labels, buckets);
            }
            buckets.add(bucket);
        }
        Vector result = new Vector();
        for (Map.Entry<Labels, List<Bucket>> entry : map.entrySet()) {
            Vector.Sample sample = new Vector.Sample(time, Quantile.quantile(q, entry.getValue()), entry.getKey());
            result.addSample(sample);
        }
        return result;
    }


    private static long durationMilliseconds(Duration offset) {
        return offset.toMillis();
    }

}
