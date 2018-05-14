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
package com.alipay.lookout.common.log;

import com.alipay.sofa.common.log.LoggerSpaceManager;
import com.alipay.sofa.common.log.MultiAppLoggerSpaceManager;

/**
 * Created by kevin.luy@alipay.com on 2017/3/6.
 */
public final class LookoutLoggerFactory {

    public static final String LOOKOUT_LOG_SPACE = "com.alipay.lookout";

    static {
        MultiAppLoggerSpaceManager.init(LookoutLoggerFactory.LOOKOUT_LOG_SPACE, null);
    }

    private LookoutLoggerFactory() {
    }

    /***
     * 获取日志对象
     *
     * @param clazz 日志的名字
     * @return 日志实现
     */
    public static org.slf4j.Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLogger(clazz.getCanonicalName());
    }

    /**
     * 获取日志对象
     *
     * @param name 日志的名字
     * @return 日志实现
     */
    public static org.slf4j.Logger getLogger(String name) {
        //从"com/alipay/lookout/log"中获取rest的日志配置并寻找对应logger对象
        if (name == null || name.isEmpty()) {
            return null;
        }
        return LoggerSpaceManager.getLoggerBySpace(name, LOOKOUT_LOG_SPACE);
    }
}
