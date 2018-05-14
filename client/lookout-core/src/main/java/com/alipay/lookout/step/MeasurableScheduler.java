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
package com.alipay.lookout.step;

import com.alipay.lookout.api.*;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.common.LookoutConstants;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kevin.luy@alipay.com on 2017/4/3.
 */
public class MeasurableScheduler extends ThreadPoolExecutor implements ScheduledExecutorService,
                                                           ScheduledService {

    private static final AtomicLong sequencer = new AtomicLong(0);

    private final Counter           activeCount;
    private final Timer             taskExecutionTime;
    private final Timer             taskExecutionDelay;
    private final Counter           skipped;

    Id                              id;
    Registry                        registry;

    public MeasurableScheduler(final Registry registry, final String name, int poolSize) {
        super(poolSize, poolSize, 0, TimeUnit.NANOSECONDS, new DelayQueue(),
            newThreadFactory(name), new AbortPolicy());
        this.registry = registry;
        Id mixinMetricId = registry.createId("lookout.scheduler." + name).withTag(
            LookoutConstants.TAG_PRIORITY_KEY, PRIORITY.LOW.name());
        MixinMetric mixinMetric = registry.mixinMetric(mixinMetricId);

        mixinMetric.gauge("queueSize", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return MeasurableScheduler.super.getQueue().size();
            }
        });
        activeCount = mixinMetric.counter("activeThreads");
        taskExecutionTime = mixinMetric.timer("taskExecutionTime");
        taskExecutionDelay = mixinMetric.timer("taskExecutionDelay");
        skipped = mixinMetric.counter("skipped");
        this.id = mixinMetricId;

    }

    private static ThreadFactory newThreadFactory(final String id) {
        return new ThreadFactory() {
            private final AtomicInteger next = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                final String name = "lookout-" + id + "-" + next.getAndIncrement();
                final Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            }
        };
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<?> t = decorateTask(
            command,
            new MeasurableScheduler.ScheduledFutureTask<Void>(command, null, triggerTime(delay,
                unit)));

        delayedExecute(t);
        return t;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if (callable == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<V> t = new MeasurableScheduler.ScheduledFutureTask<V>(callable,
            triggerTime(delay, unit));
        delayedExecute(t);
        return t;
    }

    public ScheduledFuture<?> scheduleAtFixedRateSkipIfLong(Runnable command, long initialDelay,
                                                            long period, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        RunnableScheduledFuture<?> t = decorateTask(
            command,
            new MeasurableScheduler.ScheduledFutureTask<Object>(command, null, triggerTime(
                initialDelay, unit), unit.toNanos(period), true));
        delayedExecute(t);
        return t;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
                                                  TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        RunnableScheduledFuture<?> t = decorateTask(
            command,
            new MeasurableScheduler.ScheduledFutureTask<Object>(command, null, triggerTime(
                initialDelay, unit), unit.toNanos(period)));
        delayedExecute(t);
        return t;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (delay <= 0)
            throw new IllegalArgumentException();
        RunnableScheduledFuture<?> t = decorateTask(
            command,
            new MeasurableScheduler.ScheduledFutureTask<Boolean>(command, null, triggerTime(
                initialDelay, unit), unit.toNanos(-delay)));
        delayedExecute(t);
        return t;
    }

    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
                                                          RunnableScheduledFuture<V> task) {
        return task;
    }

    private void delayedExecute(Runnable command) {
        if (isShutdown()) {
            reject(command);
            return;
        }
        // Prestart a thread if necessary. We cannot prestart it
        // running the task because the task (probably) shouldn't be
        // run yet, so thread will just idle until delay elapses.
        if (getPoolSize() < getCorePoolSize())
            prestartCoreThread();
        super.getQueue().add(command);
    }

    void reject(Runnable command) {
        super.getRejectedExecutionHandler().rejectedExecution(command, this);
    }

    /**
     * 支持调度的任务类型
     *
     * @param <V>
     */
    private class ScheduledFutureTask<V> extends FutureTask<V> implements
                                                              RunnableScheduledFuture<V> {

        /**
         * Sequence number to break ties FIFO
         */
        private final long sequenceNumber;
        /**
         * The time the task is enabled to execute in nanoTime units
         */
        private long       nextExecutionTime;
        /**
         * 大于0，表示固定速率调用；
         * 小于0，表示固定的延迟时间调用；
         */
        private final long period;

        /**
         * 前提 period>0;
         */
        private boolean    skipIfLong = false;

        /**
         * Creates a one-shot action with given nanoTime-based trigger time.
         */
        ScheduledFutureTask(Runnable r, V result, long ns) {
            super(r, result);
            this.nextExecutionTime = ns;
            this.period = 0;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * Creates a periodic action with given nano time and period.
         */
        ScheduledFutureTask(Runnable r, V result, long ns, long period) {
            this(r, result, ns, period, false);
        }

        ScheduledFutureTask(Runnable r, V result, long ns, long period, boolean skipIfLong) {
            super(r, result);
            this.nextExecutionTime = ns;
            this.period = period;
            this.sequenceNumber = sequencer.getAndIncrement();
            this.skipIfLong = skipIfLong;
        }

        /**
         * Creates a one-shot action with given nanoTime-based trigger.
         */
        ScheduledFutureTask(Callable<V> callable, long ns) {
            super(callable);
            this.nextExecutionTime = ns;
            this.period = 0;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        //delay队列判断是否过期
        public long getDelay(TimeUnit unit) {
            final long delayMillis = Math.max(nextExecutionTime - System.nanoTime(), 0L);
            return unit.convert(delayMillis, TimeUnit.NANOSECONDS);
        }

        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
                return 0;
            if (other instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
                long diff = nextExecutionTime - x.nextExecutionTime;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
                else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }

        /**
         * Returns true if this is a periodic (not a one-shot) action.
         *
         * @return true if periodic
         */
        public boolean isPeriodic() {
            return period != 0;
        }

        /**
         * Runs a periodic task.
         */
        private void runPeriodic() {
            boolean ok = ScheduledFutureTask.super.runAndReset();
            boolean down = isShutdown();
            // Reschedule if not cancelled and not shutdown or policy allows
            if (ok && !down) {
                updateNextExecutionTime();
                MeasurableScheduler.super.getQueue().add(this);
            }
            // This might have been the final executed delayed
            // task.  Wake up threads to check.
            else if (down)
                interruptIdleWorkers();
        }

        void updateNextExecutionTime() {
            long p = period;
            if (p > 0)
                nextExecutionTime += p; //如果超过了一次，但未达第二次，这样还是执行漏掉的一次。
            else
                nextExecutionTime = triggerTime(-p);//(得到正数delay)基于当前时间的校准，加上固定的延迟;

            if (skipIfLong) {
                while (nextExecutionTime < now()) {
                    nextExecutionTime += period;
                    skipped.inc();
                }
            }
        }

        /**
         * Overrides FutureTask version so as to reset/requeue if periodic.
         */
        public void run() {
            long start = System.nanoTime();
            try {
                activeCount.inc();
                long delay = start - nextExecutionTime;//实践执行时间-理论应该执行的实际点
                taskExecutionDelay.record(delay, TimeUnit.NANOSECONDS);
                // real logic
                if (isPeriodic())
                    runPeriodic();
                else
                    ScheduledFutureTask.super.run();
            } finally {
                activeCount.dec();
                taskExecutionTime.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            }
        }

    }

    /**
     * Returns the trigger time of a delayed action.(相比起止时间的延迟)；
     */
    private long triggerTime(long delay, TimeUnit unit) {
        return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
    }

    long triggerTime(long delay) {
        return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
    }

    final long now() {
        return System.nanoTime();
    }

    private long overflowFree(long delay) {
        Delayed head = (Delayed) super.getQueue().peek();
        if (head != null) {
            long headDelay = head.getDelay(TimeUnit.NANOSECONDS);
            if (headDelay < 0 && (delay - headDelay < 0))
                delay = Long.MAX_VALUE + headDelay;
        }
        return delay;
    }

    void interruptIdleWorkers() {
        //TODO interruptIdleWorkers
    }

}
