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
package com.alipay.sofa.lookout.gateway.core.ratelimit.impl;

import com.alipay.sofa.lookout.gateway.core.ratelimit.RateLimitService;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>限流算法由guava提供的RateLimiter实现, 该类负责提供参数构建它</p>
 * 这个实现依赖于一个动态的 Configuration 使用 ProviderBasedRateLimitServiceImpl 吧, 它本身不依赖于Configuration, 虽然你要提供一个 Provider 是基于Configuration的
 *
 * @author xiangfeng.xzc
 * @date 2018/10/23
 * @deprecated use {@link ProviderBasedRateLimitServiceImpl} instead
 */
@Deprecated
public class RateLimitServiceImpl implements RateLimitService {
    private static final Logger LOGGER            = LoggerFactory.getLogger(RateLimitServiceImpl.class);
    private static final String   RATELIMIT_CONFIGS = "ratelimit.configs";
    private static final String   RATELIMIT_DEFAULT = "ratelimit.default";
    private static final Splitter SPLITTER          = Splitter.on('|').trimResults().omitEmptyStrings();

    private static final int DEFAULT_RATE_LIMIT = 500;

    private final Configuration                   configuration;
    private       Map<String, RateLimiterWrapper> map                   = new ConcurrentHashMap<>();
    private       Map<String, Integer>            appLimitMap           = Collections.emptyMap();
    private       int                             defaultRatePerSeconds = DEFAULT_RATE_LIMIT;

    public RateLimitServiceImpl(Configuration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
        if (configuration instanceof EventSource) {
            ((EventSource) configuration).addEventListener(ConfigurationEvent.ANY, event -> refresh());
        }
        refresh();
    }

    private void refresh() {
        // 格式 |app1=100|app2=200|app3=300| 表示这3个app的限流是100,200,300
        String str = configuration.getString(RATELIMIT_CONFIGS, "");
        Map<String, RateLimiterWrapper> map = new ConcurrentHashMap<>();
        Map<String, Integer> appLimitMap = new HashMap<>();
        for (String item : SPLITTER.split(str)) {
            String[] ss = StringUtils.split(item, "=");
            if (ss.length != 2) {
                LOGGER.warn("invalid config {}", item);
            } else {
                RateLimiterWrapper w = new RateLimiterWrapper();
                int limit = Integer.parseInt(ss[1]);
                // <0 的值认为是不限速
                if (limit < 0) {
                    w.unlimited = true;
                } else {
                    w.rateLimiter = RateLimiter.create(limit);
                }
                appLimitMap.put(ss[0], limit);
                map.put(ss[0], w);
            }
        }

        this.defaultRatePerSeconds = configuration.getInt(RATELIMIT_DEFAULT, DEFAULT_RATE_LIMIT);
        // 这里直接重建一个map 不再原来的基础上改, 无需实时反映, 因此这个变量不是volatile
        this.map = map;
        this.appLimitMap = appLimitMap;
    }

    @Override
    public boolean tryAcquire(String key, int amount) {
        if (key == null) {
            return true;
        }
        RateLimiterWrapper w = map.get(key);
        if (w == null) {
            w = new RateLimiterWrapper();
            w.rateLimiter = RateLimiter.create(defaultRatePerSeconds);
            RateLimiterWrapper exists = map.putIfAbsent(key, w);
            w = exists != null ? exists : w;
        }
        if (w.unlimited) {
            return true;
        }
        return w.rateLimiter.tryAcquire(amount);
    }

    @Override
    public int getKeyLimit(String key) {
        return appLimitMap.getOrDefault(key, defaultRatePerSeconds);
    }

    /**
     * 目前里面只有一个属性
     */
    private static class RateLimiterWrapper {
        /**
         * 不限速
         */
        boolean     unlimited;
        RateLimiter rateLimiter;
    }

}

