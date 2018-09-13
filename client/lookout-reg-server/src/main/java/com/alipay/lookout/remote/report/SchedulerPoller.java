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
package com.alipay.lookout.remote.report;

import com.alipay.lookout.api.*;
import com.alipay.lookout.api.composite.CompositeRegistry;
import com.alipay.lookout.common.Assert;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.top.RollableTopGauge;
import com.alipay.lookout.core.AbstractRegistry;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.GaugeWrapper;
import com.alipay.lookout.core.InfoWrapper;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.jdk8.Function;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.PollableInfoWrapper;
import com.alipay.lookout.report.AbstractPoller;
import com.alipay.lookout.report.MetricObserver;
import com.alipay.lookout.report.filter.PriorityMetricFilter;
import com.alipay.lookout.spi.MetricFilter;
import com.alipay.lookout.step.MeasurableScheduler;
import com.alipay.lookout.step.ScheduledService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/2/7.
 */
public final class SchedulerPoller extends AbstractPoller<LookoutMeasurement> {
    private final Logger               logger               = LookoutLoggerFactory
                                                                .getLogger(SchedulerPoller.class);
    private ScheduledService           scheduler;

    //====>config
    private final boolean              enabled              = true;
    private MetricObserver             metricObserver;
    private final LookoutConfig        config;
    private final int                  numThreads           = 3;

    private CompositeRegistry          compositeRegistry;
    private ReScheduleSupport          reScheduleSupport;
    private final PriorityMetricsCache priorityMetricsCache = new PriorityMetricsCache();

    public static final String         PRIORITY_NAME        = "pri";

    public SchedulerPoller(MetricRegistry observerableRegistry, LookoutConfig config,
                           MetricObserver observer) {
        this(observerableRegistry, null, config, observer);
    }

    /**
     * @param observerableRegistry 观察目标
     * @param compositeRegistry    主要用于compositeRegistry场景，用于对scheduler进行自监控的注册表
     * @param config               lookout config
     * @param observer             metric observer
     */
    public SchedulerPoller(MetricRegistry observerableRegistry,
                           CompositeRegistry compositeRegistry, LookoutConfig config,
                           MetricObserver observer) {
        super(observerableRegistry);

        if (observerableRegistry instanceof AbstractRegistry) {
            ((AbstractRegistry) observerableRegistry).addListener(priorityMetricsCache);
        }

        this.config = config;
        //        if (observer == null) {
        //            observer = new HttpObserver(config, new DefaultAddressService());
        //        }
        this.compositeRegistry = compositeRegistry;
        this.metricObserver = observer;
        Assert.notNull(this.metricObserver, "metricObserver is required!");
    }

    /**
     * Start the scheduler to collect metrics data.
     */
    public void start() {
        if (scheduler == null) {
            // Setup main collection for publishing to lookup
            if (enabled) {
                logger.debug("scheduler poller is starting...");
                MetricRegistry r = compositeRegistry != null ? compositeRegistry : registry();
                scheduler = new MeasurableScheduler(r, "poller", numThreads);

                reScheduleSupport = new ReScheduleSupport(scheduler, config, registry().clock());
                reScheduleSupport.reschedulePoll(new Function<MetricFilter, Object>() {
                    @Override
                    public Object apply(MetricFilter metricFilter) {
                        collectData(metricFilter);
                        return null;
                    }
                });
                logger.debug("scheduler poller is started!");
            } else {
                logger.info("publishing is not enabled");
            }
        } else {
            logger.warn("registry already started, ignoring duplicate request");
        }
    }

    void reschedulePoll() {
        reScheduleSupport.reschedulePoll(new Function<MetricFilter, Object>() {
            @Override
            public Object apply(MetricFilter metricFilter) {
                collectData(metricFilter);
                return null;
            }
        });
    }

    /**
     * Stop the scheduler reporting remote data.
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
            logger
                .info("stopped collecting metrics every {}ms", config.stepMillis(PRIORITY.NORMAL));
        } else {
            logger.warn("registry stopped, but was never started");
        }
    }

    private void collectData(MetricFilter metricFilter) {
        // Publish to remote
        if (enabled) {
            try {
                if (metricFilter instanceof PriorityMetricFilter) {
                    poll0(metricObserver, ((PriorityMetricFilter) metricFilter).getPriority(), null);
                    return;
                }
                //else normal filter
                poll0(metricObserver, null, metricFilter);
            } catch (Throwable e) {
                logger.warn("failed to send metrics", e);
            }
        }
    }

    @Override
    public void poll(MetricObserver observer, MetricFilter metricFilter) {
        throw new UnsupportedOperationException("subscription is recommended!");
    }

    private void poll0(MetricObserver observer, PRIORITY priority, MetricFilter metricFilter) {
        //observer is disable ,stop poll
        if (observer.isEnable()) {
            Map<String, String> metadata = Maps.newHashMap();
            metadata.put(PRIORITY_NAME, priority.name());
            List<LookoutMeasurement> measurements = getMeasurements(priority, metricFilter);
            try {
                observer.update(measurements, metadata);
            } finally {
                logger.debug("send {} metrics to remote server. metrics:\n{}", measurements.size(),
                    measurements.toString());
            }
        }
    }

    private List<LookoutMeasurement> getMeasurements(PRIORITY priority, MetricFilter metricFilter) {
        //get measures from metrics of this registry
        List<LookoutMeasurement> lookoutMeasurements = new ArrayList<LookoutMeasurement>();
        Iterator<Metric> it = getMetricsIterator(priority);

        long polledTime = System.currentTimeMillis();

        while (it.hasNext()) {
            Metric metric = it.next();
            if (metricFilter != null && !metricFilter.matches(metric)) {
                continue;
            }
            //TODO refactor to filter
            if (metric instanceof InfoWrapper) {
                //ignore info
                if (config.getBoolean(LookoutConfig.LOOKOUT_AUTOPOLL_INFO_METRIC_IGNORE, true)) {
                    continue;
                }
                if (!((PollableInfoWrapper) metric).isAutoPolledAllowed(config
                    .stepMillis(PRIORITY.NORMAL))) {
                    continue;
                }
            }
            if (metric instanceof GaugeWrapper) {
                Gauge gauge = ((GaugeWrapper) metric).getOriginalOne();
                //如果是RollableTopGauge roll
                if (gauge instanceof RollableTopGauge) {
                    ((RollableTopGauge) gauge).roll(polledTime);
                    // and remove it from priorityMetricsCache.
                    it.remove();
                }
            }
            //deal with a metric
            lookoutMeasurements.add(LookoutMeasurement
                .from(metric,
                    (registry() instanceof CommonTagsAccessor) ? (CommonTagsAccessor) registry()
                        : null));
        }
        return lookoutMeasurements;
    }

    private Iterator<Metric> getMetricsIterator(PRIORITY priority) {
        Iterator<Metric> iterator;
        if (priority == null) {
            iterator = registry().iterator();
        } else {
            iterator = priorityMetricsCache.getMetricByPriority(priority).iterator();
        }
        return new MetricIterator(iterator);
    }

    class MetricIterator implements Iterator<Metric> {

        private final Iterator<Metric> iterator;

        private Iterator<Metric>       bucketCounterIterator;

        public MetricIterator(Iterator<Metric> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            if (bucketCounterIterator != null && bucketCounterIterator.hasNext()) {
                return true;
            }
            return iterator.hasNext();
        }

        @Override
        public Metric next() {
            if (bucketCounterIterator != null && bucketCounterIterator.hasNext()) {
                return bucketCounterIterator.next();
            }
            Metric metric = iterator.next();
            if (metric instanceof AbstractBucketCounter) {
                AbstractBucketCounter bucketCounter = (AbstractBucketCounter) metric;
                bucketCounterIterator = bucketCounter.iterator();
            }
            return metric;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

}
