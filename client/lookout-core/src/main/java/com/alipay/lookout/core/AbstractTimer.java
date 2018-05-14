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
package com.alipay.lookout.core;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Timer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * a template class fo  a {@link Timer}.
 */
public abstract class AbstractTimer implements Timer {

    protected final Clock clock;

    public AbstractTimer(Clock clock) {
        this.clock = clock;
    }

    @Override
    public <T> T record(Callable<T> callable) throws Exception {
        long st = clock.monotonicTime();
        try {
            return callable.call();
        } finally {
            record(clock.monotonicTime() - st, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void record(Runnable runnable) {
        long st = clock.monotonicTime();
        try {
            runnable.run();
        } finally {
            record(clock.monotonicTime() - st, TimeUnit.NANOSECONDS);
        }
    }
}
