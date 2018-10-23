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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.ResettableStep;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.config.LookoutConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可调整 step 的 Registry reactive mode;
 *
 * @author xiangfeng.xzc
 * @since 2018/7/26
 */
class ResettableStepRegistry extends StepRegistry implements ResettableStep, CommonTagsAccessor {
    /**
     * 默认的采样间隔时间
     */
    private static final long         INIT_STEP_MILLS = 30000;

    private final Map<String, String> commonTags      = new ConcurrentHashMap<String, String>();

    public ResettableStepRegistry(LookoutConfig config) {
        this(Clock.SYSTEM, config);
    }

    public ResettableStepRegistry(Clock clock, LookoutConfig config) {
        super(clock, config, INIT_STEP_MILLS);
    }

    public ResettableStepRegistry(Clock clock, LookoutConfig config, long initStep) {
        super(clock, config, initStep < 0 ? INIT_STEP_MILLS : initStep);
    }

    @Override
    public void setStep(long step) {
        super.setStep(step);
    }

    @Override
    public String getCommonTagValue(String name) {
        return commonTags.get(name);
    }

    @Override
    public void setCommonTag(String name, String value) {
        if (value == null) {
            commonTags.remove(name);
        } else {
            commonTags.put(name, value);
        }
    }

    @Override
    public void removeCommonTag(String name) {
        commonTags.remove(name);
    }

    @Override
    public Map<String, String> commonTags() {
        return commonTags;
    }
}
