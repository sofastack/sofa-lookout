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
package com.alipay.lookout.remote.step;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.common.utils.ClassUtil;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.*;
import com.alipay.lookout.report.MetricObserver;

import java.util.Collection;

/**
 * Created by kevin.luy@alipay.com on 2017/2/6.
 */
public final class LookoutRegistry extends ResettableStepRegistry implements CommonTagsAccessor {

    private SchedulerPoller               poller;
    private final MetricObserverComposite metricObserverComposite = new MetricObserverComposite();

    /**
     * Create a new instance.
     *
     * @param clock    clock
     * @param observer metric observer
     * @param config   lookout config
     */
    public LookoutRegistry(Clock clock, MetricObserver<LookoutMeasurement> observer,
                           LookoutConfig config) {
        this(clock, observer, config, null);
    }

    public LookoutRegistry(Clock clock, MetricObserver<LookoutMeasurement> observer,
                           LookoutConfig config, AddressService addressService) {
        this(clock, observer, config, addressService, -1l);
    }

    public LookoutRegistry(Clock clock, MetricObserver<LookoutMeasurement> observer,
                           LookoutConfig config, AddressService addressService, long currentStep) {
        super(clock, config, currentStep);
        if (observer == null) {
            observer = new HttpObserver(config, addressService == null ? getAddressService(config)
                : addressService, this);
        }
        addMetricObserver(observer);
        this.poller = new SchedulerPoller(this, config, metricObserverComposite);
        this.poller.start();
    }

    /***
     * Generate Lookout AddressService
     * @param config lookout config
     * @return AddressService generated
     */
    public static AddressService getAddressService(LookoutConfig config) {
        String addressServiceClassName = config.getString(LookoutConfig.ADDRESS_SERVICE_CLASS_NAME,
            DefaultAddressService.class.getName());
        return ClassUtil.newInstance(addressServiceClassName, new Class[] { String.class },
            new Object[] { config.getString(LookoutConfig.APP_NAME) });
    }

    public LookoutRegistry(LookoutConfig config) {
        this(Clock.SYSTEM, null, config);
    }

    public LookoutRegistry(LookoutConfig config, AddressService addressService) {
        this(Clock.SYSTEM, null, config, addressService);
    }

    public LookoutRegistry(MetricObserver<LookoutMeasurement> observer) {
        this(Clock.SYSTEM, observer, new LookoutConfig());
    }

    SchedulerPoller poller() {
        return this.poller;
    }

    public void destroy() {
        this.poller().stop();
    }

    public void addMetricObserver(MetricObserver metricObserver) {
        if (metricObserver == null) {
            return;
        }
        metricObserverComposite.addMetricObserver(metricObserver);
    }

    public Collection<MetricObserver> getMetricObservers() {
        return metricObserverComposite.getMetricObservers();
    }

    public void addMetricObserverMeasurementsFilter(MetricObserverMeasurementsFilter metricObserverMeasurementsFilter) {
        metricObserverComposite
            .addMetricObserverMeasurementsFilter(metricObserverMeasurementsFilter);
    }

    public MetricObserverComposite getMetricObserverComposite() {
        return metricObserverComposite;
    }
}
