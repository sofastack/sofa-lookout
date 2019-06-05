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
package com.alipay.sofa.lookout.gateway.core.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;

/**
 * 我们经常要记录一些日志, 但日志经常重复, 我们想要: 相同的日志1分钟内只记录一次, 利用ConcurrentHashMap, 可以很容易做到, 但需要记得清理不用的key/value, 否则会造成残留
 *
 * @author xiangfeng.xzc
 * @date 2018/11/30
 */
public class CoolDown {
    private final int                  interval;
    private final Cache<String, Entry> cache;

    public CoolDown(int intervalSeconds, int clearWhenIdleSeconds, int maxSize) {
        this.interval = intervalSeconds * 1000;
        this.cache = CacheBuilder.newBuilder().maximumSize(maxSize)
            .expireAfterAccess(Duration.ofSeconds(clearWhenIdleSeconds)).build();

        cache.cleanUp();
    }

    public boolean tryAcquire(String key) {
        return this.tryAcquire(key, System.currentTimeMillis());
    }

    public boolean tryAcquire(String key, long now) {
        // 这里会有一些冲突, 不过不要紧, 不是什么大问题
        Entry e = cache.getIfPresent(key);
        if (e == null) {
            e = new Entry();
            cache.put(key, e);
        }
        if (e.lastAcquireTime + interval < now) {
            e.lastAcquireTime = now;
            return true;
        }
        return false;
    }

    /**
     * TODO 就只有一个字段吗?
     */
    private static class Entry {
        long lastAcquireTime;
    }
}
