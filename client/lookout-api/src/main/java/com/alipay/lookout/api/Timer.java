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
package com.alipay.lookout.api;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public interface Timer extends Metric {
    /**
     * @param amount Duration of a single event
     * @param unit time unit
     */
    void record(long amount, TimeUnit unit);

    <T> T record(Callable<T> callable) throws Exception;

    /**
     * Executes the Runnable `f` and records the time taken.
     *
     * @param runnable Runnable task
     */
    void record(Runnable runnable);

    /**
     * get the count of all recorded events
     *
     * @return Event count
     */
    long count();

    /**
     * Get the total time of recorded events
     *
     * @return The total time of recorded events
     */
    long totalTime();
}
