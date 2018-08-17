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
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.ManualClock;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.info.AutoPollFriendlyInfo;
import com.alipay.lookout.api.info.AutoPollSuggestion;
import com.alipay.lookout.core.DefaultRegistry;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.alipay.lookout.api.info.AutoPollSuggestion.POLL_WHEN_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kevin.luy@alipay.com on 2018/5/16.
 */
public class PollableInfoWrapperTest {

    @Test
    public void testAutoPollSuggestion_POLL_WHEN_UPDATED() {
        ManualClock clock = new ManualClock();
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
        ManualClock clock = new ManualClock();
        clock.setWallTime(1);
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
        clock.setWallTime(3000);
        //让距离上次采集的间隔时间为:(3000-1)ms, 大于2000，所以继续 poll
        Assert.assertTrue(pollableInfoWrapper.isAutoPolledAllowed(2000));
    }

    @Test
    public void test_measure() {
        ManualClock clock = new ManualClock();
        DefaultRegistry r = new DefaultRegistry(clock);
        PollableInfoWrapper w = new PollableInfoWrapper(r.createId("info"),
            new AutoPollFriendlyInfo() {
                @Override
                public AutoPollSuggestion autoPollSuggest() {
                    return AutoPollSuggestion.POLL_WHEN_UPDATED;
                }

                @Override
                public long lastModifiedTime() {
                    return 10;
                }

                @Override
                public Object value() {
                    return "i_am_info";
                }
            }, clock);
        clock.setWallTime(20L);
        Indicator indicator = w.measure();
        assertThat(indicator).isNotNull();
        assertThat(indicator.getTimestamp()).isEqualTo(20L);
        assertThat(indicator.measurements()).hasSize(1);
        assertThat(((Measurement<?>) indicator.measurements().iterator().next()).value())
            .isEqualTo("i_am_info");
    }

    @Test
    public void test_measure_timeout() {
        Clock clock = Clock.SYSTEM;
        DefaultRegistry r = new DefaultRegistry(clock);
        PollableInfoWrapper w = new PollableInfoWrapper(r.createId("info"),
            new AutoPollFriendlyInfo() {
                @Override
                public AutoPollSuggestion autoPollSuggest() {
                    return AutoPollSuggestion.POLL_WHEN_UPDATED;
                }

                @Override
                public long lastModifiedTime() {
                    return 10;
                }

                @Override
                public Object value() {
                    try {
                        Thread.sleep(1100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "i_am_info";
                }
            }, clock);
        Indicator indicator = w.measure();
        assertThat(indicator).isNotNull();
        assertThat(indicator.measurements()).hasSize(1);
        assertThat(((Measurement<?>) indicator.measurements().iterator().next()).value())
            .isEqualTo("i_am_info");
    }
}
