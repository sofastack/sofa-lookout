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
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.info.AutoPollFriendlyInfo;
import com.alipay.lookout.api.info.AutoPollSuggestion;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.core.InfoWrapper;

/**
 * Created by kevin.luy@alipay.com on 2017/4/10.
 */
public final class PollableInfoWrapper extends InfoWrapper {
    //采集建议
    private long               lastPolledTime = -1l;
    private AutoPollSuggestion suggestion;

    public PollableInfoWrapper(Id id, Info info, Clock clock) {
        super(id, info, clock);
        if (info instanceof AutoPollFriendlyInfo) {
            this.suggestion = ((AutoPollFriendlyInfo) info).autoPollSuggest();
        }
    }

    public boolean isAutoPolledAllowed(long stepMills) {
        if (suggestion == null) {
            return true;
        }
        if (suggestion == AutoPollSuggestion.NEVEL_AUTO_POLL) {
            return false;
        }
        //        if (suggestion == AutoPollSuggestion.POLL_ONCE) {
        //            if (lastPolledTime > 0) {
        //                return false;
        //            }
        //            lastPolledTime = System.currentTimeMillis();//第一次执行后的时间
        //            return true;
        //        }
        if (suggestion == AutoPollSuggestion.POLL_WHEN_UPDATED) {
            //最近发生更新了
            if (lastPolledTime <= ((AutoPollFriendlyInfo) info).lastModifiedTime()) {
                lastPolledTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        if (suggestion.intervalMills() < stepMills) {
            return true;//忽略建议间隔
        }
        // 默认会有小于step的误差；
        if (lastPolledTime <= 0
            || (System.currentTimeMillis() - lastPolledTime >= suggestion.intervalMills())) {
            lastPolledTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public Indicator measure() {
        long start = System.currentTimeMillis();
        try {
            return super.measure();
        } finally {
            //可能很耗时,超过1s先日志警告！！
            long duration = System.currentTimeMillis() - start;
            if (duration > 1000) {
                logger.warn("Info metric id:{} value method invoke too long(duration:{})!",
                    duration);
            }
        }
    }
}
