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
package com.alipay.lookout.starter.support.reg;

import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.AddressService;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.MetricObserver;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class LookoutServerRegistryFactory implements
                                         MetricsRegistryFactory<LookoutRegistry, LookoutConfig> {
    private static final Logger                      logger = LookoutLoggerFactory
                                                                .getLogger(LookoutServerRegistryFactory.class);

    private List<MetricObserver<LookoutMeasurement>> metricObservers;

    private AddressService                           addressService;

    public LookoutServerRegistryFactory(List<MetricObserver<LookoutMeasurement>> metricObservers) {
        this.metricObservers = metricObservers;
    }

    public LookoutServerRegistryFactory(List<MetricObserver<LookoutMeasurement>> metricObservers,
                                        AddressService addressService) {
        this.metricObservers = metricObservers;
        this.addressService = addressService;
    }

    @Override
    public LookoutRegistry get(LookoutConfig metricConfig) {
        LookoutRegistry lookoutRegistry = new LookoutRegistry(metricConfig, this.addressService);
        //add observers to lookoutRegistry
        if (!CollectionUtils.isEmpty(metricObservers)) {
            for (MetricObserver observer : metricObservers)
                lookoutRegistry.addMetricObserver(observer);
            logger.info("add metricObservers:{} to lookout registry.", metricObservers);
        }
        return lookoutRegistry;
    }
}
