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

/**
 * Measurement type Created by kevin.luy@alipay.com on 2017/2/14.
 */
public enum Statistic {
    rate,
    /**
     * Rate per second for calls to record.
     */
    count,

    /**
     * The maximum amount recorded.
     */
    max,

    /**
     * The sum of the amounts recorded.
     */
    totalAmount,

    /**
     * buckets of the amounts recorded
     */
    buckets,

    //    /**
    //     * The sum of the squares of the amounts recorded.
    //     */
    //    totalOfSquares,

    /**
     * elapse time per execution
     */
    elapPerExec,

    /**
     * The sum of the times recorded.
     */
    totalTime,

    /**
     * Duration of a running task.
     */
    duration,

    /**
     * info. not only numerical value;
     */
    info
}
