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

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author xiangfeng.xzc
 * @date 2018/12/3
 */
public interface RateLimitConfigProvider {
    /**
     * 获取配置, key是app, value通常是一个int(如果确定是这样的话那么就改成Integer吧!), 表示该key对应的最大QPS
     * @return
     */
    Map<String, Integer> getConfigs();

    /**
     * 设置监听器, 会覆盖掉之前的
     * @param consumer
     */
    void setListener(Consumer<Map<String, Integer>> consumer);
}
