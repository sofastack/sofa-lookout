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
package com.alipay.lookout.api.info;

import com.alipay.lookout.common.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2017/2/22.
 */
public class AutoPollSuggestion {
    //不要自动采集
    public static final AutoPollSuggestion NEVEL_AUTO_POLL   = new AutoPollSuggestion(-1);
    //只自动采集一次即可,暂时无法确定是否汇报成功;
    //    public static final AutoPollSuggestion POLL_ONCE = new AutoPollSuggestion(-2);
    public static final AutoPollSuggestion POLL_WHEN_UPDATED = new AutoPollSuggestion(-3);

    private long                           intervalMills     = 0;

    /**
     * suggest auto poll interval
     *
     * @param interval interval
     * @param timeUnit time unit
     */
    public AutoPollSuggestion(long interval, TimeUnit timeUnit) {
        Assert.checkArg(interval >= -3, "interval is illegal!");
        Assert.checkArg(timeUnit != null, "timeUnit is required!");
        this.intervalMills = timeUnit.toMillis(interval);
    }

    private AutoPollSuggestion(long intervalMills) {
        this.intervalMills = intervalMills;
    }

    public long intervalMills() {
        return intervalMills;
    }

}
