package com.alipay.lookout.starter.support.actuator;

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LookoutMetricServicesImpl
 *
 * @author yangguanchao
 * @since 2018/06/04
 */
public class LookoutSpringBootMetricsImpl implements CounterService, GaugeService {

    private static final String LOOKOUT_SUFFIX = "lookout.";

    private static final String LOOKOUT_COUNTER_PREFIX = LOOKOUT_SUFFIX + "counter.";

    private static final String LOOKOUT_GAUGE_PREFIX = LOOKOUT_SUFFIX + "gauge.";

    private final Registry registry;

    private final ConcurrentMap<String, SimpleLookoutGauge> gauges = new ConcurrentHashMap<String, SimpleLookoutGauge>();

    public LookoutSpringBootMetricsImpl(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void increment(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        metricName = wrapName(LOOKOUT_COUNTER_PREFIX, metricName);
        Id id = this.registry.createId(metricName);
        Counter counter = this.registry.counter(id);
        counter.inc();
    }

    @Override
    public void decrement(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        metricName = wrapName(LOOKOUT_COUNTER_PREFIX, metricName);
        Id id = this.registry.createId(metricName);
        Counter counter = this.registry.counter(id);
        counter.dec();
    }

    @Override
    public void reset(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        metricName = wrapName(LOOKOUT_COUNTER_PREFIX, metricName);
        Id id = this.registry.createId(metricName);
        this.registry.removeMetric(id);
    }

    @Override
    public void submit(String metricName, double value) {
        if (StringUtils.isBlank(metricName)) {
            return;
        }
        //名字优雅点
        metricName = wrapName(LOOKOUT_GAUGE_PREFIX, metricName);

        SimpleLookoutGauge gauge = this.gauges.get(metricName);
        if (gauge == null) {
            SimpleLookoutGauge newGauge = new SimpleLookoutGauge(value);
            gauge = this.gauges.putIfAbsent(metricName, newGauge);
            if (gauge == null) {
                Id id = this.registry.createId(metricName);
                this.registry.gauge(id, newGauge);
                return;
            }
        }
        gauge.setValue(value);
    }

    /***
     * 构造 lookout 前缀信息
     *
     * @param metricName 名称
     * @param prefix lookout 前缀
     * @return 标识信息
     */
    private String wrapName(String prefix, String metricName) {
        if (StringUtils.isBlank(metricName)) {
            throw new RuntimeException("Metric name can't be blank!");
        }
        if (metricName.startsWith(prefix)) {
            return metricName;
        }
        return prefix + metricName;
    }

    private final static class SimpleLookoutGauge implements Gauge<Double> {

        private volatile double value;

        private SimpleLookoutGauge(double value) {
            this.value = value;
        }

        @Override
        public Double value() {
            return this.value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

}
