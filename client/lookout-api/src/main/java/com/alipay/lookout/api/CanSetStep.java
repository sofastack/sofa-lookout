package com.alipay.lookout.api;

/**
 * 暴露一个接口用于修改 step
 *
 * @author xiangfeng.xzc
 * @date 2018/7/26
 */
public interface CanSetStep {
    /**
     * 设置新的step
     *
     * @param step 新的步长, 必须>0
     */
    void setStep(long step);
}
