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
package com.alipay.sofa.lookout.boot;

import com.alipay.sofa.lookout.ark.support.SofaArkEmbedUtils;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-09 14:57
 **/
@SpringBootApplication
public class ServerBootstrap {
    private static final String APP_NAME = "metrics-server";

    public static void main(String[] args) {
        boolean sofaArkStarted = SofaArkEmbedUtils.isSofaArkStarted();
        if (sofaArkStarted) {
            if (!SofaArkEmbedUtils.isAppEnabled(APP_NAME)) {
                return;
            }
        }
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ServerBootstrap.class);
        if (sofaArkStarted) {
            SofaArkEmbedUtils.enhance(APP_NAME, builder);
        }
        builder.build().run(args);

    }
}
