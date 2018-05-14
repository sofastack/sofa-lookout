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
package com.alipay.lookout.report;

import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/2/16.
 */
public class LogObserver implements MetricObserver<LookoutMeasurement> {
    private Logger logger = LookoutLoggerFactory.getLogger("LOOKOUT-LOG-REPORTER");

    public LogObserver() {
    }

    public LogObserver(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void update(List<LookoutMeasurement> measures, Map<String, String> metadata) {
        logger.info(measures.toString());
    }
}
