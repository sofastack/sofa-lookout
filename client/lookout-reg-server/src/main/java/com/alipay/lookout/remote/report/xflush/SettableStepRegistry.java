package com.alipay.lookout.remote.report.xflush;

import com.alipay.lookout.api.CanSetStep;
import com.alipay.lookout.api.Clock;
import com.alipay.lookout.core.CommonTagsAccessor;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.StepRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可调整 step 的 Registry
 *
 * @author xiangfeng.xzc
 * @date 2018/7/26
 */
public class SettableStepRegistry extends StepRegistry implements CanSetStep, CommonTagsAccessor {
    /**
     * 默认的采样间隔时间
     */
    private static final long INIT_STEP_MILLS = 10000;

    private final Map<String, String> commonTags = new ConcurrentHashMap<String, String>();

    public SettableStepRegistry(Clock clock, LookoutConfig config) {
        super(clock, config, INIT_STEP_MILLS);
    }

    public SettableStepRegistry(Clock clock, LookoutConfig config, long initStepMills) {
        super(clock, config, initStepMills);
    }

    @Override
    public void setStep(long step) {
        super.setStep(step);
    }

    @Override
    public String getCommonTagValue(String name) {
        return commonTags.get(name);
    }

    @Override
    public void setCommonTag(String name, String value) {
        if (value == null) {
            commonTags.remove(name);
        } else {
            commonTags.put(name, value);
        }
    }

    @Override
    public void removeCommonTag(String name) {
        commonTags.remove(name);
    }

    @Override
    public Map<String, String> commonTags() {
        return commonTags;
    }
}
