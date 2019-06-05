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
package com.alipay.sofa.lookout.gateway.core.token;

import com.alipay.sofa.lookout.gateway.core.common.Constants;
import com.alipay.sofa.lookout.gateway.core.common.WebfluxUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author xiangfeng.xzc
 * @date 2018/10/9
 */
public final class LookoutTokenResolveUtils {
    private LookoutTokenResolveUtils() {
    }

    /**
     * <p>从headers中解析出 LookoutToken, 按如下的规则:
     * <ol>
     * <li>检查头 X-Lookout-Token, 如果非空则它的值就是token</li>
     * <li>检查头 Authorization, 如果是一个basic认证, 那么username就是token. 即 "Basic base64(token:)" 的格式</li>
     * </ol>
     * </p>
     *
     * @param headers
     * @return
     */
    public static String getLookoutToken(ServerRequest.Headers headers) {
        String token = WebfluxUtils.getHeaderValue(headers, Constants.TOKEN_HEADER_NAME);
        if (token == null) {
            String authorization = WebfluxUtils.getHeaderValue(headers, "Authorization");
            // 按照下面的方式反解出token即可
            // Authorization: Basic: base64(token:)
            if (StringUtils.startsWith(authorization, "Basic ")) {
                String base64 = authorization.substring(6);
                String usernameAndPassword = new String(Base64.getUrlDecoder().decode(base64),
                    StandardCharsets.UTF_8);
                String username = StringUtils.substringBefore(usernameAndPassword, ":");
                // 如果 username 是 lookout 那么以 password 作为token
                // 否则 username 用户名作为token
                if ("lookout".equals(username)) {
                    String password = StringUtils.substringAfter(usernameAndPassword, ":");
                    token = password;
                } else {
                    token = username;
                }
            }
        }
        return token;
    }
}
