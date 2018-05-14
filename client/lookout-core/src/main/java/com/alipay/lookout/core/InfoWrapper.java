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
package com.alipay.lookout.core;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import org.slf4j.Logger;

/**
 * Created by kevin.luy@alipay.com on 2017/2/22.
 */
public class InfoWrapper<I, Y extends Info<I>> implements Info<I>, Metric {
    protected static final Logger logger       = LookoutLoggerFactory.getLogger(Info.class);
    public static final String    EMPTY_STRING = "";
    protected Y                   info;
    private Clock                 clock;
    private Id                    id;

    public InfoWrapper(Id id, Y info, Clock clock) {
        this.id = id;
        this.info = info;
        this.clock = clock;
    }

    @Override
    public Indicator measure() {
        String text = EMPTY_STRING;
        I value = null;
        try {
            value = value();
        } catch (Throwable e) {
            //可能出异常
            logger.warn("Info metric id:s% value fail:s%!", id, e.getMessage());
            text = "ERROR:" + e.getMessage();
        }
        return new Indicator(clock.wallTime(), id(), value != null ? value : text);
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public I value() {
        return info.value();
    }

}
