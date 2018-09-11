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
 * Histogram.
 * Track the sample distribution of events. An example would be the response sizes for requests
 * hitting and http server.
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public interface DistributionSummary extends BucketCounter {

    String BUCKET_TAG_NAME = "_bucket";

    /**
     * Updates the statistics with the specified amount.
     *
     * @param amount Amount for an event being measured. For example, if the size in bytes of responses
     *               from a server. If the amount is less than 0 the value will be dropped.
     */
    void record(long amount);

    /**
     * The number of times that record has been called
     *
     * @return count
     */
    long count();

    /**
     * The total amount of all recorded events
     *
     * @return total
     */
    long totalAmount();

}
