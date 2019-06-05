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
import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 屏蔽Configuration2的依赖, 换成由Provider提供配置来源
 * @author xiangfeng.xzc
 * @date 2018/12/3
 */
public class ProviderBasedRateLimitServiceImpl implements RateLimitService {
    private final    RateLimitConfigProvider provider;
    private volatile Map<String, Entry>      map = new ConcurrentHashMap<>();
    private volatile int                     defaultRate;

    public ProviderBasedRateLimitServiceImpl(RateLimitConfigProvider provider) {
        this.provider = Preconditions.checkNotNull(provider);
        provider.setListener(this::refresh);
        this.refresh(provider.getConfigs());
    }

    private void refresh(Map<String, Integer> configs) {
        Map<String, Entry> map = new ConcurrentHashMap<>();
        for (Map.Entry<String, Integer> e : configs.entrySet()) {
            int rate = e.getValue();
            if ("*".equals(e.getKey()) || "all".equals(e.getKey())) {
                this.defaultRate = rate;
            } else {
                Entry entry;
                if (rate > 0) {
                    entry = Entry.limited(rate);
                } else {
                    entry = Entry.unlimited();
                }
                map.put(e.getKey(), entry);
            }
        }
        this.map = map;
    }

    @Override
    public boolean tryAcquire(String key, int amount) {
        Entry entry = getEntry(key);
        if (entry.unlimited) {
            return true;
        }
        return entry.rateLimiter.tryAcquire(amount);
    }

    @Override
    public int getKeyLimit(String key) {
        return getEntry(key).rate;
    }

    protected Entry getEntry(String key) {
        Entry entry = this.map.get(key);
        if (entry == null) {
            // 如果该key没有配置限速, 那么就lazy创建
            int rate = defaultRate;
            if (rate > 0) {
                entry = Entry.limited(rate);
            } else {
                entry = Entry.unlimited();
            }
            Entry exists = this.map.putIfAbsent(key, entry);
            entry = exists == null ? entry : exists;
        }
        return entry;
    }

    private static class Entry {
        static final Entry       UNLIMITED = new Entry(true, -1, null);
        final        boolean     unlimited;
        final        int         rate;
        final        RateLimiter rateLimiter;

        Entry(boolean unlimited, int rate, RateLimiter rateLimiter) {
            this.unlimited = unlimited;
            this.rate = rate;
            this.rateLimiter = rateLimiter;
        }

        static Entry unlimited() {
            return UNLIMITED;
        }

        static Entry limited(int rate) {
            return new Entry(false, rate, RateLimiter.create(rate));
        }
    }
}
