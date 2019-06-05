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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author xiangfeng.xzc
 * @date 2018/12/3
 */
public class ConfigurationRateLimitConfigProvider implements RateLimitConfigProvider {
    private static final Logger                     LOGGER            = LoggerFactory
                                                                          .getLogger(ConfigurationRateLimitConfigProvider.class);
    private final Configuration                     configuration;
    private volatile Map<String, Integer>           configs           = Collections.emptyMap();
    private static final String                     RATELIMIT_CONFIGS = "ratelimit.configs";
    private static final Splitter                   SPLITTER          = Splitter.on('|')
                                                                          .trimResults()
                                                                          .omitEmptyStrings();
    private volatile Consumer<Map<String, Integer>> listener;

    public ConfigurationRateLimitConfigProvider(Configuration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);

        // 这里解释一下, 一般情况下我们面向 org.apache.commons.configuration2.Configuration 编程
        // 而 Configuration 接口没有扩展 EventSource 接口, 但实际上, 几乎所有 Configuration 的实现都是 AbstractConfiguration 的子类, 而它实现了EventSource接口.
        // 通过 EventSource 接口就可以监听配置项的变化(搭配ops-config使用)

        if (configuration instanceof EventSource) {
            ((EventSource) configuration).addEventListener(ConfigurationEvent.ANY, event -> refresh());
        }
        refresh();
    }

    private void refresh() {
        // 配置项格式: app1=100|app2=200|app3=300|all=-1
        // 表示app1,app2,app3的最大qps分别是100,200,300 对于其他应用则为-1, 即不限速

        Map<String, Integer> configs = new HashMap<>();
        // 默认全部不限速
        String str = configuration.getString(RATELIMIT_CONFIGS, "all=-1");
        for (String item : SPLITTER.split(str)) {
            String[] ss = StringUtils.split(item, "=");
            if (ss.length != 2) {
                LOGGER.warn("invalid config {}", item);
            } else {
                configs.put(ss[0], Integer.parseInt(ss[1]));
            }
        }
        this.configs = configs;
        Consumer<Map<String, Integer>> listener = this.listener;
        if (listener != null) {
            listener.accept(configs);
        }
    }

    @Override
    public Map<String, Integer> getConfigs() {
        return configs;
    }

    @Override
    public void setListener(Consumer<Map<String, Integer>> listener) {
        this.listener = listener;
    }
}
