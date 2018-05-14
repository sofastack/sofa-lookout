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
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.common.utils.PriorityTagUtil;
import com.alipay.lookout.core.AbstractRegistry;
import com.alipay.lookout.core.GaugeWrapper;
import com.alipay.lookout.core.common.NewMetricFunction;
import com.alipay.lookout.core.config.LookoutConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kevin.luy@alipay.com on 2017/3/26.
 */
public class StepRegistry extends AbstractRegistry {
    protected final Clock clock;                //未对齐时间

    //for LookoutMixinMetric
    private long          fixedStepMillis = -1l;

    public StepRegistry(Clock clock, LookoutConfig config) {
        this(clock, config, -1l);
    }

    /**
     * for LookoutMixinMetric
     *
     * @param clock
     * @param config
     * @param fixedStepMillis
     */
    public StepRegistry(Clock clock, LookoutConfig config, long fixedStepMillis) {
        super(clock, config);
        this.clock = clock;
        this.fixedStepMillis = fixedStepMillis;
    }

    private LookoutConfig getLookoutConfig() {
        return getConfig();
    }

    @Override
    protected Counter newCounter(Id id) {
        return new LookoutCounter(id, clock, getStepMillis(id));
    }

    @Override
    protected DistributionSummary newDistributionSummary(Id id) {
        return new LookoutDistributionSummary(id, clock, getStepMillis(id));
    }

    private long getStepMillis(Id id) {
        if (fixedStepMillis > 0) {
            return fixedStepMillis;
        }
        return getLookoutConfig().stepMillis(PriorityTagUtil.resolve(id.tags()));
    }

    @Override
    protected Timer newTimer(Id id) {
        return new LookoutTimer(id, clock, getStepMillis(id));
    }

    @Override
    protected Metric newMixinMetric(Id id) {
        long stepSize = getStepMillis(id);
        return new LookoutMixinMetric(id, new StepRegistry(clock, getLookoutConfig(), stepSize),
            stepClock(stepSize));
    }

    @Override
    public <T extends Number> Gauge<T> gauge(Id id, final Gauge<T> gauge) {
        return (Gauge<T>) computeIfAbsent(id, new NewMetricFunction<Metric>() {
            @Override
            public Metric apply(Id id) {
                return new GaugeWrapper(id, gauge, stepClock(getStepMillis(id)));
            }

            @Override
            public Metric noopMetric() {
                return null;
            }
        });
    }

    @Override
    public <I, Y extends Info<I>> Info info(Id id, final Y info) {
        return (Info<I>) computeIfAbsent(id, new NewMetricFunction<Metric>() {
            @Override
            public Metric apply(Id id) {
                return new PollableInfoWrapper(id, info, stepClock(getStepMillis(id)));
            }

            @Override
            public Metric noopMetric() {
                return null;
            }
        });
    }

    private static final ConcurrentHashMap<Long, StepClock> stepClockCache = new ConcurrentHashMap<Long, StepClock>();

    /**
     * 步长对齐时钟
     *
     * @param stepMills
     * @return
     */
    private StepClock stepClock(long stepMills) {
        StepClock stepClock = stepClockCache.get(stepMills);
        if (stepClock == null) {
            stepClock = new StepClock(clock, stepMills);
            StepClock old = stepClockCache.putIfAbsent(stepMills, stepClock);
            stepClock = old != null ? old : stepClock;//old first
        }
        return stepClock;
    }
}
