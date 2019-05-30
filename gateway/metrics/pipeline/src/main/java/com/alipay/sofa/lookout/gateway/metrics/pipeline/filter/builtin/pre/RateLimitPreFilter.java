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

import com.alipay.sofa.lookout.gateway.core.common.LogUtils;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.AbstractFilter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.ratelimit.RateLimitService;
import com.alipay.sofa.lookout.gateway.core.token.LookoutTokenService;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import com.alipay.sofa.lookout.server.common.AccessToken;

import java.util.Map;

/**
 * TODO 将限流做得统一一点, 针对metrics的话, 限流主要是限制每个app的上报频率. 如果trace也需要限流, 那么也应该提出一个类似app的概念.
 *
 * @author xiangfeng.xzc
 * @date 2018/11/29
 */
public class RateLimitPreFilter extends AbstractFilter<RawMetric> {
    private static final Filter.FilterResult RATE_LIMITED = new Filter.FilterResult("RATE_LIMITED");
    private final LookoutTokenService        lookoutTokenService;
    private final RateLimitService           rateLimitService;

    public RateLimitPreFilter(LookoutTokenService lookoutTokenService,
                              RateLimitService rateLimitService) {
        super("TokenPreFilter");
        this.lookoutTokenService = lookoutTokenService;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public String type() {
        return "metric";
    }

    @Override
    public Filter.FilterResult test(RawMetric rm, Map<String, ?> filterContext) {
        if (rm.getSourceType() == SourceType.STANDARD) {
            return SUCCESS;
        }
        String token = rm.getHead().getToken();
        if (token == null) {
            return SUCCESS;
        }
        boolean acquire = acquireRate(token);
        if (!acquire) {
            if (rm.getHead().getDebugId() != null) {
                LogUtils.DEBUG_LOGGER
                    .info("debugId={} exceed ratelimit", rm.getHead().getDebugId());
            }
            return RATE_LIMITED;
        }
        return SUCCESS;
    }

    private boolean acquireRate(String token) {
        AccessToken tokenDto = lookoutTokenService.findAccessTokenInCache(token);
        if (tokenDto != null) {
            String app = tokenDto.getApp();
            return rateLimitService.tryAcquire(app, 1);
        }
        return true;
    }
}
