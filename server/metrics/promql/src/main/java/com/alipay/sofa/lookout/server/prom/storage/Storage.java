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
package com.alipay.sofa.lookout.server.prom.storage;

import com.alipay.sofa.lookout.server.prom.storage.query.Querier;

/**
 * store time serial data.
 *
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-28 12:20
 **/
public interface Storage {

    Querier querier();

    /**
     * whether this storage service supports an aggregate query.
     *
     * @return
     */
    boolean isAggregatable(String aggregator);

    boolean isHealthy();

    void start();

    long startTime();

    void close();

}
