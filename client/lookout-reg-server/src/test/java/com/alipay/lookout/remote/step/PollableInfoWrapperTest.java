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
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.info.AutoPollFriendlyInfo;
import com.alipay.lookout.api.info.AutoPollSuggestion;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.alipay.lookout.api.info.AutoPollSuggestion.POLL_WHEN_UPDATED;

/**
 * Created by kevin.luy@alipay.com on 2018/5/16.
 */
public class PollableInfoWrapperTest {

    @Test
    public void testAutoPollSuggestion_POLL_WHEN_UPDATED() {
        Clock clock = new MockClock();
        Registry r = new DefaultRegistry(clock);
        PollableInfoWrapper pollableInfoWrapper = new PollableInfoWrapper(r.createId("name"),
            new AutoPollFriendlyInfo() {

                @Override
                public AutoPollSuggestion autoPollSuggest() {
                    return POLL_WHEN_UPDATED;
                }

                @Override
                public long lastModifiedTime() {
                    return 1;
                }

                @Override
                public String value() {
                    return "x";
                }
            }, clock);

        Assert.assertTrue(pollableInfoWrapper.isAutoPolledAllowed(1000));
        Assert.assertTrue(pollableInfoWrapper.isAutoPolledAllowed(9000));
    }

    @Test
    public void testAutoPollSuggestionAfterInterval() {
        Clock clock = new MockClock();
        ((MockClock) clock).setWallTime(1);
        Registry r = new DefaultRegistry(clock);
        PollableInfoWrapper pollableInfoWrapper = new PollableInfoWrapper(r.createId("name"),
            new AutoPollFriendlyInfo() {

                @Override
                public AutoPollSuggestion autoPollSuggest() {
                    return new AutoPollSuggestion(2, TimeUnit.SECONDS);
                }

                @Override
                public long lastModifiedTime() {
                    return 1;
                }

                @Override
                public String value() {
                    return "x";
                }
            }, clock);

        Assert.assertTrue(pollableInfoWrapper.isAutoPolledAllowed(1000));
        ((MockClock) clock).setWallTime(3000);
        //让距离上次采集的间隔时间为:(3000-1)ms, 大于2000，所以继续 poll
        Assert.assertTrue(pollableInfoWrapper.isAutoPolledAllowed(2000));
    }
}
