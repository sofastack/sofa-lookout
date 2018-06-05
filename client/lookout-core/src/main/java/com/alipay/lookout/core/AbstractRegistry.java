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

import com.alipay.lookout.api.*;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.api.info.AutoPollFriendlyInfo;
import com.alipay.lookout.api.info.AutoPollSuggestion;
import com.alipay.lookout.api.info.Info;
import com.alipay.lookout.common.Assert;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.core.common.NewMetricFunction;
import com.alipay.lookout.core.config.MetricConfig;
import com.alipay.lookout.event.MetricRegistryListener;
import com.alipay.lookout.spi.DefaultMetricsImporterLocator;
import com.alipay.lookout.spi.MetricsImporter;
import com.alipay.lookout.spi.MetricsImporterLocator;
import org.apache.commons.configuration2.MapConfiguration;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.alipay.lookout.core.config.LookoutConfig.LOOKOUT_MAX_METRICS_NUMBER;

/**
 * Base class of a simple registry
 */
public abstract class AbstractRegistry extends MetricRegistry {
    protected final Logger                      logger        = LookoutLoggerFactory
                                                                  .getLogger(getClass());

    private final ConcurrentHashMap<Id, Metric> metrics;
    private MetricConfig                        config;

    private final List<MetricRegistryListener>  listeners     = new CopyOnWriteArrayList<MetricRegistryListener>();

    private volatile boolean                    maxNumWarning = true;

    /**
     * @param clock  Clock used for performing all timing measurements.
     * @param config can be null,then default config will be used;
     */
    public AbstractRegistry(Clock clock, MetricConfig config) {
        super(clock);
        this.metrics = new ConcurrentHashMap<Id, Metric>();
        Assert.notNull(config, "config is null!");
        this.config = config;
    }

    public void setConfig(MetricConfig config) {
        this.config = config;
    }

    @Override
    public void registerExtendedMetrics() {
        MetricsImporterLocator locator = new DefaultMetricsImporterLocator();
        for (MetricsImporter metricsImporter : locator.locate()) {
            metricsImporter.register(this);
        }

        MixinMetric mixinMetric = mixinMetric(createId("lookout.reg"));
        mixinMetric.gauge("size", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return metrics.size();
            }
        });
        mixinMetric.gauge("max.size", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return config.getInt(LOOKOUT_MAX_METRICS_NUMBER,
                    MetricConfig.DEFAULT_MAX_METRICS_NUM);
            }
        });

        // register config info
        info(createId("lookout.config"), new AutoPollFriendlyInfo<Map<String, Object>>() {
            @Override
            public AutoPollSuggestion autoPollSuggest() {
                return AutoPollSuggestion.POLL_WHEN_UPDATED;
            }

            @Override
            public long lastModifiedTime() {
                return -1;
            }

            @Override
            public Map<String, Object> value() {
                if (config instanceof MapConfiguration) {
                    return ((MapConfiguration) config).getMap();
                }
                return null;
            }
        });
    }

    public <M extends MetricConfig> M getConfig() {
        return (M) config;
    }

    /*
     * id 已经注册过，抛异常
     *
     * @param metric
     * @param <M>
     * @return
     */
    @Override
    public void register(final Metric metric) {
        // Assert.checkArg(metric instanceof Gauge, "Only Gauge support now");
        try {
            Metric m = computeIfAbsent(metrics, metric.id(), new NewMetricFunction<Metric>() {
                @Override
                public Metric apply(Id id) {
                    return metric;
                }

                @Override
                public Metric noopMetric() {
                    return null;
                }
            });
            Assert.checkArg(m.getClass() == metric.getClass(),
                "duplicated metric! id:" + metric.id() + " is already existed!  existed one:"
                        + metric);
        } catch (Exception e) {
            propagate(e);
        }
    }

    @Override
    public <T extends Number> Gauge<T> gauge(Id id, final Gauge<T> gauge) {
        return (Gauge<T>) computeIfAbsent(metrics, id, new NewMetricFunction<Metric>() {
            @Override
            public Metric apply(Id id) {
                return new GaugeWrapper(id, gauge, clock());
            }

            @Override
            public Metric noopMetric() {
                return NoopGauge.INSTANCE;
            }
        });
    }

    @Override
    public <I, Y extends Info<I>> Info info(Id id, final Y info) {
        return (Info<I>) computeIfAbsent(metrics, id, new NewMetricFunction<Metric>() {
            @Override
            public Metric apply(Id id) {
                return wrapperInfo(id, info);
            }

            @Override
            public Metric noopMetric() {
                return NoopInfo.INSTANCE;
            }
        });
    }

    protected <I, Y extends Info<I>> InfoWrapper<I, Y> wrapperInfo(Id id, Y info) {
        return new InfoWrapper(id, info, clock());
    }

    /**
     * only support gauge or info now!
     * for not singleton and temp life;
     *
     * @param id
     */
    @Override
    public void removeMetric(Id id) {
        //        Assert.isTrue(metrics.get(id) instanceof Gauge
        //                                 || metrics.get(id) instanceof Info,
        //            String.format("this id: %s is not gauge or info!", id));
        Metric metric = metrics.remove(id);
        if (metric != null) {
            onMetricRemoved(metric);
        }
    }

    /**
     * Create a new counter instance for a given id.
     *
     * @param id metricId.
     * @return New counter instance.
     */
    protected abstract Counter newCounter(Id id);

    @Override
    public final Counter counter(Id id) {
        try {
            Assert.notNull(id, "id");

            Metric m = computeIfAbsent(metrics, id, new NewMetricFunction<Counter>() {
                @Override
                public Counter apply(Id id) {
                    return newCounter(id);
                }

                @Override
                public Counter noopMetric() {
                    return NoopCounter.INSTANCE;
                }
            });

            if (!(m instanceof Counter)) {
                logTypeError(id, Counter.class, m.getClass());
                m = NoopCounter.INSTANCE;
            }
            return (Counter) m;
        } catch (Exception e) {
            propagate(e);
            return NoopCounter.INSTANCE;
        }
    }

    /**
     * compatible with jdk6
     */
    private <T extends Metric> T computeIfAbsent(ConcurrentHashMap<Id, Metric> map, Id id,
                                                 NewMetricFunction<? extends Metric> f) {
        Metric m = map.get(id);
        if (m == null) {
            //如果metrics过多了，则给个noop，并给出提示;
            if (map.size() >= config.getInt(LOOKOUT_MAX_METRICS_NUMBER,
                MetricConfig.DEFAULT_MAX_METRICS_NUM)) {
                if (maxNumWarning) {
                    logger
                        .warn(
                            "metrics number reach max limit: {}! Do not record this new metric(id:{}).",
                            config.getInt(LOOKOUT_MAX_METRICS_NUMBER,
                                MetricConfig.DEFAULT_MAX_METRICS_NUM), id);
                    maxNumWarning = false;
                }
                return (T) f.noopMetric();
            }
            //if the key exists,this execution is useless!
            Metric tmp = f.apply(id);
            m = map.putIfAbsent(id, tmp);
            if (m == null) {
                //first register
                m = tmp;
                onMetricAdded(tmp);
            }
        }
        return (T) m;
    }

    protected final <T extends Metric> T computeIfAbsent(Id id,
                                                         NewMetricFunction<? extends Metric> f) {
        return computeIfAbsent(metrics, id, f);
    }

    private void logTypeError(Id id, Class<? extends Metric> desired, Class<? extends Metric> found) {
        final String dtype = desired.getName();
        final String ftype = found.getName();
        final String msg = String.format("cannot access '%s' as a %s, it already exists as a %s",
            id, dtype, ftype);
        propagate(new IllegalStateException(msg));
    }

    /**
     * Create a new distribution summary instance for a given id.
     *
     * @param id metricId.
     * @return New distribution summary instance.
     */
    protected abstract DistributionSummary newDistributionSummary(Id id);

    @Override
    public final DistributionSummary distributionSummary(Id id) {
        try {
            Assert.notNull(id, "id");

            Metric m = computeIfAbsent(metrics, id, new NewMetricFunction<DistributionSummary>() {
                @Override
                public DistributionSummary apply(Id id) {
                    return newDistributionSummary(id);
                }

                @Override
                public DistributionSummary noopMetric() {
                    return NoopDistributionSummary.INSTANCE;
                }
            });

            if (!(m instanceof DistributionSummary)) {
                logTypeError(id, DistributionSummary.class, m.getClass());
                m = NoopDistributionSummary.INSTANCE;
            }
            return (DistributionSummary) m;
        } catch (Exception e) {
            propagate(e);
            return NoopDistributionSummary.INSTANCE;
        }
    }

    /**
     * Create a new timer instance for a metric id.
     *
     * @param id metricId.
     * @return New timer instance.
     */
    protected abstract Timer newTimer(Id id);

    @Override
    public final Timer timer(Id id) {
        try {
            Metric m = computeIfAbsent(metrics, id, new NewMetricFunction<Timer>() {
                @Override
                public Timer apply(Id id) {
                    return newTimer(id);
                }

                @Override
                public Timer noopMetric() {
                    return NoopTimer.INSTANCE;
                }
            });
            if (!(m instanceof Timer)) {
                logTypeError(id, Timer.class, m.getClass());
                m = NoopTimer.INSTANCE;
            }
            return (Timer) m;
        } catch (Exception e) {
            propagate(e);
            return NoopTimer.INSTANCE;
        }
    }

    @Override
    public final <X extends Metric> X get(Id id) {
        return (X) metrics.get(id);
    }

    @Override
    public final Iterator<Metric> iterator() {
        return metrics.values().iterator();
    }

    @Override
    public MixinMetric mixinMetric(Id id) {
        try {
            Assert.notNull(id, "id");
            Metric m = computeIfAbsent(metrics, id, new NewMetricFunction<Metric>() {
                @Override
                public Metric apply(Id id) {
                    return newMixinMetric(id);
                }

                @Override
                public Metric noopMetric() {
                    return NoopMixinMetric.INSTANCE;
                }
            });

            if (!(m instanceof MixinMetric)) {
                logTypeError(id, MixinMetric.class, m.getClass());
                m = NoopMixinMetric.INSTANCE;
            }
            return (MixinMetric) m;
        } catch (Exception e) {
            propagate(e);
            return NoopMixinMetric.INSTANCE;
        }
    }

    /***
     * Get the unmodifiable cached metrics
     * @return cached metrics
     */
    public Map<Id, Metric> getMetrics() {
        return Collections.unmodifiableMap(this.metrics);
    }

    protected abstract Metric newMixinMetric(Id id);

    /**
     * Adds a {@link MetricRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricRegistryListener listener) {
        listeners.add(listener);
        for (Metric metric : metrics.values()) {
            notifyListenerOfAddedMetric(listener, metric);
        }
    }

    /**
     * Removes a {@link MetricRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(MetricRegistryListener listener) {
        listeners.remove(listener);
    }

    private void onMetricAdded(Metric metric) {
        for (MetricRegistryListener listener : listeners) {
            notifyListenerOfAddedMetric(listener, metric);
        }
    }

    private void onMetricRemoved(Metric metric) {
        for (MetricRegistryListener listener : listeners) {
            notifyListenerOfRemovedMetric(metric, listener);
        }
    }

    private void notifyListenerOfAddedMetric(MetricRegistryListener listener, Metric metric) {
        listener.onAdded(metric);
    }

    private void notifyListenerOfRemovedMetric(Metric metric, MetricRegistryListener listener) {
        listener.onRemoved(metric);
    }
}
