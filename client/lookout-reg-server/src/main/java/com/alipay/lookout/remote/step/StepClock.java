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
import com.alipay.lookout.api.Id;

/**
 * wall time 对齐
 * Created by kevin.luy@alipay.com on 2017/2/6.
 */
final class StepClock implements Clock {

    private final StepRegistry stepRegistry;
    private final Id           id;

    StepClock(StepRegistry stepRegistry, Id id) {
        this.stepRegistry = stepRegistry;
        this.id = id;
    }

    @Override
    public long wallTime() {
        //support step refresh.
        long step = stepRegistry.getStepMillis(id);
        return stepRegistry.clock().wallTime() / step * step;
    }

    @Override
    public long monotonicTime() {
        return stepRegistry.clock().monotonicTime();
    }
}
