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
package com.alipay.lookout.remote.step;

import com.alipay.lookout.api.*;
import com.alipay.lookout.step.StepLong;
import com.alipay.lookout.step.StepValue;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kevin.luy@alipay.com on 2017/2/6.
 */
public class LookoutDistributionSummary extends BucketDistributionSummary implements DistributionSummary, ResettableStep {

    private final Id       id;
    private final StepLong count;
    private final StepLong total;
    private final StepLong max;

    LookoutDistributionSummary(Id id, Clock clock, long step) {
        this.id = id;
        this.count = new StepLong(0L, clock, step);
        this.total = new StepLong(0L, clock, step);
        this.max = new StepLong(0L, clock, step);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Indicator measure() {
        double rate = count.pollAsRate();
        long timestamp = count.timestamp();

        Indicator indicator = new Indicator(timestamp, id);
        indicator.addMeasurement(new Measurement(Statistic.rate.name(), rate));
        indicator.addMeasurement(newMeasurement(Statistic.totalAmount.name(), total));
        indicator.addMeasurement(newMaxMeasurement(Statistic.max.name(), max));
        return indicator;
    }

    private Measurement<Double> newMeasurement(String mid, StepValue v) {
        return new Measurement<Double>(mid, v.pollAsRate());
    }

    private Measurement<Long> newMaxMeasurement(String mid, StepLong max) {
        return new Measurement<Long>(mid, max.poll());
    }

    @Override
    public void setStep(long step) {
        count.setStep(step);
        total.setStep(step);
        max.setStep(step);
    }

    @Override
    public void record(long amount) {
        if (amount > 0) {
            count.getCurrent().incrementAndGet();
            total.getCurrent().addAndGet(amount);
            refreshMax(max.getCurrent(), amount);
            recordBucket(id, amount);
        }
    }

    private void refreshMax(AtomicLong maxValue, long v) {
        for (long max = maxValue.get(); (v > max && !maxValue.compareAndSet(max, v)); max = maxValue
                .get()) {
        }
    }

    @Override
    public long count() {
        return count.poll();
    }

    @Override
    public long totalAmount() {
        return total.poll();
    }

}
