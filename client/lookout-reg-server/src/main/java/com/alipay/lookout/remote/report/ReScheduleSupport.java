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

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.jdk8.Function;
import com.alipay.lookout.report.filter.PriorityMetricFilter;
import com.alipay.lookout.spi.MetricFilter;
import com.alipay.lookout.step.ScheduledService;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kevin.luy@alipay.com on 2017/2/23.
 */
final class ReScheduleSupport {
    private static final Logger    logger              = LookoutLoggerFactory
                                                           .getLogger(SchedulerPoller.class);

    static final String            THREAD_NAME_SPLITOR = "-";
    private Set<TaskResult>        taskResults;

    private final ScheduledService scheduler;
    private final LookoutConfig    config;
    private final Clock            clock;

    public ReScheduleSupport(ScheduledService scheduler, LookoutConfig config, Clock clock) {
        this.scheduler = scheduler;
        this.config = config;
        this.clock = clock;
    }

    //refresh synchronized
    public synchronized void reschedulePoll(Function<MetricFilter, Object> function) {
        if (taskResults != null) {
            //end old schedule tasks
            for (TaskResult result : taskResults) {
                result.close();
            }
        }
        //start new schedule tasks
        Set<TaskResult> tasks = new HashSet<TaskResult>(3);
        tasks.add(schedulePoll(PRIORITY.NORMAL, config.stepMillis(PRIORITY.NORMAL),
            new PriorityMetricFilter(PRIORITY.NORMAL), function));
        tasks.add(schedulePoll(PRIORITY.HIGH, config.stepMillis(PRIORITY.HIGH),
            new PriorityMetricFilter(PRIORITY.HIGH), function));
        tasks.add(schedulePoll(PRIORITY.LOW, config.stepMillis(PRIORITY.LOW),
            new PriorityMetricFilter(PRIORITY.LOW), function));
        this.taskResults = tasks;
    }

    public TaskResult schedulePoll(final PRIORITY priority, final long stepMillis,
                                   final MetricFilter metricFilter,
                                   final Function<MetricFilter, Object> function) {

        final TaskResult taskResult = new TaskResult();

        Future future = scheduler.scheduleAtFixedRateSkipIfLong(new Runnable() {
            @Override
            public void run() {
                if (taskResult.getEnable().get()) {//确保任务Cancel失败，导致重复采集;
                    String oldThreadName = Thread.currentThread().getName();
                    try {
                        Thread.currentThread().setName(
                            oldThreadName + THREAD_NAME_SPLITOR + priority + THREAD_NAME_SPLITOR
                                    + stepMillis);
                        function.apply(metricFilter);
                    } finally {
                        Thread.currentThread().setName(oldThreadName);
                    }
                }
            }
        }, getInitialDelay(stepMillis, clock), stepMillis, TimeUnit.MILLISECONDS);

        taskResult.setFuture(future);
        logger.info("started collecting and reporting priority:{} metrics every {} ", priority,
            stepMillis);
        return taskResult;
    }

    class TaskResult {
        private final AtomicBoolean enable = new AtomicBoolean(true);
        private Future              future;

        public void setFuture(Future future) {
            this.future = future;
        }

        public Future getFuture() {
            return future;
        }

        public AtomicBoolean getEnable() {
            return enable;
        }

        public void close() {
            enable.getAndSet(false);
            future.cancel(true);
        }
    }

    /**
     * 初始执行的延迟，避免过于靠近stepsize的边界，导致偏离过大；
     *
     * @param stepSize TimeUnit.MILLISECONDS
     * @return TimeUnit.MILLISECONDS
     */
    static long getInitialDelay(long stepSize, Clock clock) {
        long now = clock.wallTime();
        long stepStart = now / stepSize * stepSize;
        // 1/10的时间段，作为过于靠近的判断依据
        long offset = stepSize / 10;

        long delay = now - stepStart;
        if (delay < offset) { //当前时间过于靠近低边界,则启动延时补偿个10%的的stepSize；
            return delay + offset;
        } else if (delay > stepSize - offset) {//当前时间过于靠近高边界，那么再收缩10%的stepSize
            return now - stepStart - offset;
        } else { //如果距离左右边界距离合适
            return delay;
        }
    }

}
