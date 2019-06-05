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
 * Distribution summary implementation for the no-op registry.
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
enum NoopDistributionSummary implements DistributionSummary {

    /**
     * Singleton instance.
     */
    INSTANCE;

    @Override
    public Id id() {
        return NoopId.INSTANCE;
    }

    @Override
    public void record(long amount) {
    }

    @Override
    public Indicator measure() {
        return null;
    }

    @Override
    public long count() {
        return 0L;
    }

    @Override
    public long totalAmount() {
        return 0L;
    }

    @Override
    public void buckets(long[] buckets) {

    }
}
