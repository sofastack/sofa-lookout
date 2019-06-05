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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.filter.builtin.pre;

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.AbstractFilter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.token.LookoutTokenService;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetricHead;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import com.alipay.sofa.lookout.server.common.AccessToken;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO 时间敏感性 防止拖慢流程 统计数量, 这个filter依赖token 需要放在CommonPreFilter之后
 *
 * @author xiangfeng.xzc
 * @date 2018/11/23
 */
public class ImporterStatsFilter extends AbstractFilter<RawMetric> {
    private final Registry            registry;
    private final LookoutTokenService lookoutTokenService;

    public ImporterStatsFilter(Registry registry, LookoutTokenService lookoutTokenService) {
        this.registry = registry;
        this.lookoutTokenService = lookoutTokenService;
    }

    @Override
    public String type() {
        return "metric";
    }

    @Override
    public Filter.FilterResult test(RawMetric rm, Map<String, ?> filterContext) {
        // 1. 按照app 维度
        // TODO 但不是每种上报方式会带app过来, 目前有的: standard, prometheus(我们会做检查), opentsdb("tag在body里, 除非解析, 否则无法拿到, 不过我们可以根据token来获取")

        RawMetricHead head = rm.getHead();
        // 默认以standard app名为准
        String app = head.getStandardAppName();
        String token = null;

        // standard模式不需要token
        if (rm.getSourceType() != SourceType.STANDARD) {
            token = head.getToken();
            if (token != null) {
                AccessToken tokenInfo = lookoutTokenService.findAccessTokenInCache(token);
                if (tokenInfo != null) {
                    if (app == null) {
                        app = tokenInfo.getApp();
                    }
                } else {
                    // tokenInfo==null 意味着token是非法的 设置为null
                    // 因为这里是从cache里获取的数据, 可能出现这种情况
                    token = null;
                }
            }
        }

        if (app == null) {
            app = rm.getExtraTags().get("app");
        }

        if (app != null) {
            // 这里上报的数据会被打上gateway自己的tags(即覆盖)

            // 统计app上报次数
            Map<String, String> tags = new HashMap<>();
            tags.put("client_app", app);
            tags.put("source_type", rm.getSourceType().toString().toLowerCase());
            if (token != null) {
                tags.put("token", token);
            }
            Id id = registry.createId("metrics.upload.stats", tags);
            Counter counter = registry.counter(id);
            counter.inc();
        }

        // 按其他维度统计

        return SUCCESS;
    }
}
