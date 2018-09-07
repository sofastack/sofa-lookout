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
import com.google.common.base.Preconditions;

/**
 * proactive mode; different fixed steps by different priorities. reactive mode; step is determined by collector;
 * <p>
 * Created by kevin.luy@alipay.com on 2017/3/26.
 */
public class StepRegistry extends AbstractRegistry {
    protected final Clock clock;                   //未对齐时间

    //for LookoutMixinMetric
    protected volatile long currentStepMillis = -1L;

    private boolean proactive = true;

    public StepRegistry(Clock clock, LookoutConfig config) {
        this(clock, config, -1L);
    }

    /**
     * for LookoutMixinMetric
     *
     * @param clock
     * @param config
     * @param currentStepMillis
     */
    public StepRegistry(Clock clock, LookoutConfig config, long currentStepMillis) {
        super(clock, config);
        this.clock = clock;
        this.currentStepMillis = currentStepMillis;
    }

    public void setProactive(boolean proactive) {
        this.proactive = proactive;
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
        LookoutDistributionSummary distributionSummary = new LookoutDistributionSummary(id, clock, getStepMillis(id));
        distributionSummary.setRegistry(this);
        return distributionSummary;
    }

    protected long getStepMillis(Id id) {
        if (proactive) {
            return getLookoutConfig().stepMillis(PriorityTagUtil.resolve(id.tags()));
        }
        //reactive mode
        Preconditions.checkState(currentStepMillis > 0);
        return currentStepMillis;
    }

    @Override
    protected Timer newTimer(Id id) {
        return new LookoutTimer(id, clock, getStepMillis(id));
    }

    @Override
    protected Metric newMixinMetric(Id id) {
        long stepSize = getStepMillis(id);
        //mixin 的 step registry ，mode 不需要切换了,因为有了 stepClock;
        return new LookoutMixinMetric(id, new StepRegistry(clock, getLookoutConfig(), stepSize),
                stepClock(id));
    }

    @Override
    public <T extends Number> Gauge<T> gauge(Id id, final Gauge<T> gauge) {
        return (Gauge<T>) computeIfAbsent(id, new NewMetricFunction<Metric>() {
            @Override
            public Metric apply(final Id id) {
                return new GaugeWrapper(id, gauge, stepClock(id));
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
                return new PollableInfoWrapper(id, info, stepClock(id));
            }

            @Override
            public Metric noopMetric() {
                return null;
            }
        });
    }

    /**
     * 步长对齐时钟
     *
     * @param id
     * @return
     */
    private StepClock stepClock(Id id) {
        return new StepClock(this, id);
    }

    /**
     * reactive mode. 重新设置step, 修改所有的metric
     *
     * @param step
     */
    protected synchronized void setStep(long step) {
        if (this.currentStepMillis == step) {
            return;
        }
        this.currentStepMillis = step;
        for (Metric m : this) {
            if (m instanceof ResettableStep) {
                ((ResettableStep) m).setStep(step);
            }
        }
    }

    /**
     * reactive mode. 获取当前使用的采样间隔时间
     *
     * @return
     */
    public long getCurrentStepMillis() {
        return currentStepMillis;
    }
}
