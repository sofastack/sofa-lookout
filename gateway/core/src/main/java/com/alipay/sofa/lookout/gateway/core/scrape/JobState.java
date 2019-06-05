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
package com.alipay.sofa.lookout.gateway.core.scrape;

import java.time.Duration;
import java.time.Instant;

/**
 * 一次完整的scrape状态记录
 *
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-04 11:04
 **/
public class JobState {
    private String   endpoint        = null;
    private Instant  lastScrapedTime = Instant.MIN;
    private boolean  successful      = false;
    /**
     * optional
     **/
    private String   error           = null;
    /**
     * job抓取总耗时
     */
    private Duration duration        = Duration.ZERO;

    public Instant getLastScrapedTime() {
        return lastScrapedTime;
    }

    public void setLastScrapedTime(Instant lastScrapedTime) {
        this.lastScrapedTime = lastScrapedTime;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
