package com.alipay.lookout.starter.support.reader;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.MetricRegistry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.event.MetricRegistryListener;
import com.alipay.lookout.starter.support.converter.IndicatorConvert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.reader.MetricReader;

import java.util.*;

/**
 * LookoutRegistryMetricReader
 * A Spring Boot {@link MetricReader} that reads metrics from a Lookout {@link MetricRegistry}
 *
 * @author yangguanchao
 * @since 2018/01/24
 */
public class LookoutRegistryMetricReader implements MetricReader, MetricRegistryListener {

    private final DefaultRegistry defaultRegistry;

    public LookoutRegistryMetricReader(DefaultRegistry registry) {
        //lookout defaultRegistry
        this.defaultRegistry = registry;
        //事件监听
       this.defaultRegistry.addListener(this);
    }

    /***
     * Spring Boot 关注标准接口的几个实现
     * @param metricName 名称
     * @return 对应的 Actuator Metric
     */
    @Override
    public Metric<?> findOne(String metricName) {
        if (StringUtils.isBlank(metricName)) {
            return null;
        }
        //标准 Actuator 实现
        Id id = this.defaultRegistry.createId(metricName);
        List<Metric> metricList = findMetricsById(id);
        if (metricList != null && metricList.size() > 0) {
            //由于和底层 lookout 的映射关系,todo 先默认返回第一个
            return metricList.get(0);
        } else {
            return null;
        }
    }

    private List<Metric> findMetricsById(Id id) {
        com.alipay.lookout.api.Metric lookoutMetric = this.defaultRegistry.get(id);
        if (lookoutMetric == null) {
            return null;
        }
        Indicator indicator = lookoutMetric.measure();
        return IndicatorConvert.convertFromIndicator(indicator);
    }

    @Override
    public Iterable<Metric<?>> findAll() {
        final Iterator<com.alipay.lookout.api.Metric> lookoutIt = this.defaultRegistry.iterator();
        return new Iterable<Metric<?>>() {
            @Override
            public Iterator<Metric<?>> iterator() {
                List<Metric<?>> metricsRes = new LinkedList<Metric<?>>();
                while (lookoutIt.hasNext()) {
                    com.alipay.lookout.api.Metric lookoutMetric = lookoutIt.next();
                    Id id = lookoutMetric.id();
                    Collection<Metric> metricList = findMetricsById(id);
                    if (metricList != null && metricList.size() > 0) {
                        for (Metric metric : metricList) {
                            metricsRes.add(metric);
                        }
                    }
                }
                return metricsRes.iterator();
            }
        };
    }

    @Override
    public long count() {
        Map<Id, com.alipay.lookout.api.Metric> lookoutDefaultMetrics = this.defaultRegistry.getMetrics();
        if (lookoutDefaultMetrics == null || lookoutDefaultMetrics.size() == 0) {
            return 0;
        } else {
            return lookoutDefaultMetrics.size();
        }
    }

    @Override
    public void onRemoved(com.alipay.lookout.api.Metric metric) {

    }

    @Override
    public void onAdded(com.alipay.lookout.api.Metric metric) {

    }

}
