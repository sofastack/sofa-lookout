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
package com.alipay.lookout.starter.support;

import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.sofa.common.log.global.Slite2LogPathInit;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import static com.alipay.sofa.common.log.Constants.LOG_LEVEL_PREFIX;
import static com.alipay.sofa.common.log.Constants.LOG_PATH;

/**
 * 预先处理 Lookout Client 日志空间
 * 的日志空间
 * Created by kevin.luy@alipay.com on 2017/3/13.
 */
public class LookoutApplicationContextInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        //设置日志打印路径
        String loggingPath = applicationContext.getEnvironment().getProperty(LOG_PATH);
        Slite2LogPathInit.initLoggingPath(loggingPath);

        //如果存在，设置space日志level
        String spaceLevel = applicationContext.getEnvironment().getProperty(
            LOG_LEVEL_PREFIX + LookoutLoggerFactory.LOOKOUT_LOG_SPACE);
        if (!StringUtils.isEmpty(spaceLevel)) {
            System.setProperty(LOG_LEVEL_PREFIX + LookoutLoggerFactory.LOOKOUT_LOG_SPACE,
                spaceLevel);
        }
    }
}
