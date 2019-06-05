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
import com.alipay.sofa.lookout.gateway.core.token.LookoutTokenService;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;

import java.util.Map;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/29
 */
public class TokenPreFilter extends AbstractFilter<RawMetric> {
    private static final Filter.FilterResult TOKEN_IS_EMPTY   = new Filter.FilterResult(
                                                                  "token is empty");
    private static final Filter.FilterResult TOKEN_IS_INVALID = new Filter.FilterResult(
                                                                  "TOKEN_IS_INVALID");
    private final LookoutTokenService        lookoutTokenService;

    public TokenPreFilter(LookoutTokenService lookoutTokenService) {
        super("TokenPreFilter");
        this.lookoutTokenService = lookoutTokenService;
    }

    @Override
    public String type() {
        return "metric";
    }

    @Override
    public Filter.FilterResult test(RawMetric rm, Map<String, ?> filterContext) {
        // STANDARD 来自标准客户端, 不检查
        if (rm.getSourceType() == SourceType.STANDARD || !rm.isPushMode()) {
            return SUCCESS;
        }

        // token检查失败我们就不记录日志了 因为客户端会收到4xx的response 很容易就知道原因了
        String token = rm.getHead().getToken();
        if (token == null) {
            if (rm.getHead().getDebugId() != null) {
                LogUtils.DEBUG_LOGGER.info("debugId={} token is empty", rm.getHead().getDebugId());
            }
            return TOKEN_IS_EMPTY;
        }

        boolean validToken = checkToken(token);
        if (!validToken) {
            if (rm.getHead().getDebugId() != null) {
                LogUtils.DEBUG_LOGGER
                    .info("debugId={} token is invalid", rm.getHead().getDebugId());
            }
            return TOKEN_IS_INVALID;
        }
        return SUCCESS;
    }

    private boolean checkToken(String token) {
        return lookoutTokenService.checkToken(token);
    }
}
