package com.alipay.sofa.lookout.ark.support;

import com.alipay.sofa.ark.support.startup.SofaArkBootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiangfeng.xzc
 * @date 2019/1/2
 */
public final class BootUtils {
    public static final String SOFA_ARK_MARK = "com.alipay.sofa.lookout.all.boot.LookoutAllBootstrap";

    private static final Logger LOGGER = LoggerFactory.getLogger(BootUtils.class);

    private BootUtils() {}

    private static final String BIZ_CLASSLOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

    public static boolean isSofaArkStarted() {
        Class<?> bizClassloader = SofaArkBootstrap.class.getClassLoader().getClass();
        return BIZ_CLASSLOADER.equals(bizClassloader.getCanonicalName());
    }
}
