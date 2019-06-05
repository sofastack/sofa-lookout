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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 存放一些全局通用的logger
 *
 * @author xiangfeng.xzc
 * @date 2018/11/26
 */
public final class LogUtils {
    private LogUtils() {
    }

    public static final Logger DIGEST_LOGGER        = LoggerFactory.getLogger("agent.digest");                      // NOPMD

    /**
     * 记录丢失的metric的日志, 方便排查问题
     */
    public static final Logger MISSING_LOGGER       = LoggerFactory.getLogger("agent.missing");                     // NOPMD

    /**
     * 当请求携带了debug id时用它来打印日志
     */
    public static final Logger DEBUG_LOGGER         = LoggerFactory.getLogger("debug");                             // NOPMD
    public static final Logger READER_LOGGER        = LoggerFactory.getLogger("reader");                            // NOPMD

    public static final Logger SCRAPE_DIGEST_LOGGER = LoggerFactory
                                                        .getLogger("com.alipay.lookout.gateway.core.scrape.digest"); //NOPMD

}
