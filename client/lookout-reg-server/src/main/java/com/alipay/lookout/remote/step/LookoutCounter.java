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
import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Statistic;
import com.alipay.lookout.step.StepLong;

/**
 * 时间步长内的累计值（而DefaultCounter是从启动时到当前的统计）
 * Created by kevin.luy@alipay.com on 2017/2/6.
 */
public class LookoutCounter implements Counter, CanSetStep {

    private final Id       id;
    private final StepLong value;

    LookoutCounter(Id id, Clock clock, long step) {
        this.id = id;
        this.value = new StepLong(0L, clock, step);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public Indicator measure() {
        double rate = value.pollAsRate();
        return new Indicator(value.timestamp(), id).addMeasurement(Statistic.count.name(),
            value.previous()).addMeasurement(Statistic.rate.name(), rate);
    }

    @Override
    public void inc() {
        value.getCurrent().incrementAndGet();
    }

    @Override
    public void inc(long amount) {
        value.getCurrent().addAndGet(amount);
    }

    public void dec() {
        dec(1);
    }

    @Override
    public void dec(long n) {
        inc(-n);
    }

    @Override
    public long count() {
        return value.poll();
    }

    @Override
    public void setStep(long step) {
        this.value.setStep(step);
    }
}
