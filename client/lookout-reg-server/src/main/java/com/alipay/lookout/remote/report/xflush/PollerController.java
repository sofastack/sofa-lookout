package com.alipay.lookout.remote.report.xflush;

import com.alipay.lookout.api.Gauge;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.common.top.RollableTopGauge;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.GaugeWrapper;
import com.alipay.lookout.core.InfoWrapper;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.google.common.primitives.Longs;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/26
 */
public class PollerController {
    private static final int DEFAULT_SLOT_COUNT = 100;

    /**
     * 比较器 按照cursor倒序排序
     */
    private static final Comparator<SlotItem> COMPARATOR = new Comparator<SlotItem>() {
        @Override
        public int compare(SlotItem o1, SlotItem o2) {
            return Longs.compare(o2.getCursor(), o1.getCursor());
        }
    };

    private final SettableStepRegistry registry;

    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * 采样间隔时间, step将会扩散到registry包含的所有实现了 CanSetStep 接口的 metric
     */
    private long step = -1;

    private ScheduledFuture<?> taskFuture;

    private volatile MetricCache metricCache;

    /**
     * 槽数量
     */
    private int slotCount;


    public PollerController(SettableStepRegistry registry) {
        this(registry, DEFAULT_SLOT_COUNT);
    }


    public PollerController(SettableStepRegistry registry, int initSlotCount) {
        this.registry = registry;
        ThreadFactory tf = new BasicThreadFactory.Builder().namingPattern("PollerController %d").build();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, tf);
        update(registry.getFixedStepMillis(), initSlotCount);
    }

    /**
     * 因为step或slowCount的调整, 导致需要重建 MetricCache, 这个方法尽量重用 旧的cache里的slot, 减少数据丢失
     *
     * @param step
     * @param slotCount
     * @return
     */
    private MetricCache createCache(long step, int slotCount) {
        MetricCache oldCache = this.metricCache;
        if (oldCache != null) {
            return new MetricCache(oldCache, step, slotCount);
        } else {
            return new MetricCache(step, slotCount);
        }
    }

    /**
     * 修改slotCount
     *
     * @param slotCount
     */
    public synchronized void setSlotCount(int slotCount) {
        if (slotCount <= 0) {
            throw new IllegalArgumentException("slotCount must greater than 0");
        }
        if (this.slotCount == slotCount) {
            return;
        }
        this.slotCount = slotCount;
        if (this.step <= 0) {
            return;
        }
        this.metricCache = createCache(step, slotCount);
    }

    /**
     * 修改频率
     *
     * @param step
     */
    public synchronized void setStep(long step) {
        if (this.step == step) {
            return;
        }
        this.step = step;
        if (taskFuture != null) {
            taskFuture.cancel(true);
            taskFuture = null;
        }

        // stop
        if (step <= 0) {
            this.metricCache = null;
            return;
        }

        this.metricCache = createCache(step, slotCount);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                MetricCache cache = PollerController.this.metricCache;
                if (cache == null) {
                    return;
                }
                List<MetricDto> data = convertToDto(registry, true, registry);
                cache.add(data);
            }
        };
        // 这里应该不需要对齐时间
        taskFuture = scheduledExecutorService.scheduleAtFixedRate(
            runnable,
            0,
            step,
            TimeUnit.MILLISECONDS);
    }

    /**
     * 获得下一批是数据
     *
     * @param successCursors 上一次成功的curcors
     * @return
     */
    public List<SlotItem> getNextData(Set<Long> successCursors) {
        MetricCache array = this.metricCache;
        if (array == null) {
            return Collections.emptyList();
        } else {
            List<SlotItem> data = array.getNextData(successCursors);
            Collections.sort(data, COMPARATOR);
            return data;
        }
    }

    public void clear() {
        MetricCache array = this.metricCache;
        if (array != null) {
            array.clear();
        }
    }

    public void destroy() {
        scheduledExecutorService.shutdownNow();
    }

    /**
     * 更新该 poller 的配置
     *
     * @param newStep
     * @param newSlotCount
     */
    public synchronized void update(long newStep, int newSlotCount) {
        setSlotCount(newSlotCount);
        setStep(newStep);
        registry.setStep(newStep);
    }

    /**
     * 获取槽数量
     *
     * @return
     */
    public int getSlotCount() {
        return slotCount;
    }

    /**
     * 获取采样步长
     *
     * @return
     */
    public long getStep() {
        return step;
    }

    private static List<MetricDto> convertToDto(Iterable<Metric> iterable, boolean ignoreInfo,
                                                CommonTagsAccessor commonTagsAccessor) {

        List<MetricDto> results = new ArrayList<MetricDto>();

        long polledTime = System.currentTimeMillis();
        Iterator<Metric> it = iterable.iterator();
        while (it.hasNext()) {
            Metric metric = it.next();
            // TODO 更好地处理info型metric
            if (ignoreInfo && metric instanceof InfoWrapper) {
                continue;
            }
            if (metric instanceof GaugeWrapper) {
                Gauge gauge = ((GaugeWrapper) metric).getOriginalOne();
                if (gauge instanceof RollableTopGauge) {
                    ((RollableTopGauge) gauge).roll(polledTime);
                    it.remove();
                }
            }

            MetricDto dto = new MetricDto();
            LookoutMeasurement lookoutMeasurement = LookoutMeasurement.from(metric,
                commonTagsAccessor);
            dto.setName(lookoutMeasurement.metricId().name());
            dto.setTimestamp(lookoutMeasurement.getDate().getTime());
            dto.setMetrics(lookoutMeasurement.getValues());
            dto.setTags(lookoutMeasurement.getTags());
            results.add(dto);
        }
        return results;
    }

}
