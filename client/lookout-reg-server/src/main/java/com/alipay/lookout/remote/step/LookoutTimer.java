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

import com.alipay.lookout.api.CanSetStep;
import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Statistic;
import com.alipay.lookout.api.Timer;
import com.alipay.lookout.step.StepLong;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kevin.luy@alipay.com on 2017/2/6.
 */
class LookoutTimer implements Timer, CanSetStep {

    private final Id id;
    private final Clock clock;
    private final StepLong count;
    private final StepLong total;
    private final StepLong max;

    LookoutTimer(Id id, Clock clock, long step) {
        this.id = id;
        this.clock = clock;
        this.count = new StepLong(0L, clock, step);
        this.total = new StepLong(0L, clock, step);
        this.max = new StepLong(0L, clock, step);
    }

    @Override
    public void setStep(long step) {
        count.setStep(step);
        total.setStep(step);
        max.setStep(step);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Indicator measure() {
        long timestamp = count.timestamp();
        Indicator indicator = new Indicator(timestamp, id);

        double totalSeconds = total.poll() * 1e-9;
        long countValue = count.poll();
        double epe = countValue <= 0 ? 0 : totalSeconds / countValue;

        indicator.addMeasurement(new Measurement(Statistic.elapPerExec.name(), epe));
        indicator.addMeasurement(new Measurement(Statistic.totalTime.name(), totalSeconds));
        indicator.addMeasurement(newMaxMeasurement(Statistic.max.name(), max));
        return indicator;
    }

    private Measurement newMaxMeasurement(String mName, StepLong v) {
        return new Measurement(mName, v.poll() / 1e9);
    }

    @Override
    public void record(long amount, TimeUnit unit) {
        if (amount > 0) {
            long nanos = unit.toNanos(amount);
            count.getCurrent().incrementAndGet();
            total.getCurrent().addAndGet(nanos);
            refreshMax(max.getCurrent(), nanos);
        }
    }

    private void refreshMax(AtomicLong maxValue, long v) {
        for (long max = maxValue.get(); (v > max && !maxValue.compareAndSet(max, v)); max = maxValue
            .get()) {
        }
    }

    @Override
    public <T> T record(Callable<T> callable) throws Exception {
        long start = clock.monotonicTime();
        try {
            return callable.call();
        } finally {
            record(clock.monotonicTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void record(Runnable runnable) {
        long start = clock.monotonicTime();
        try {
            runnable.run();
        } finally {
            record(clock.monotonicTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public long count() {
        return count.poll();
    }

    @Override
    public long totalTime() {
        return total.poll();
    }

}
