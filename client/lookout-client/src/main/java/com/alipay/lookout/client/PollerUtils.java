package com.alipay.lookout.client;

import com.alipay.lookout.api.Clock;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.report.poller.Listener;
import com.alipay.lookout.remote.report.poller.MetricsHttpExporter;
import com.alipay.lookout.remote.report.poller.PollerController;
import com.alipay.lookout.remote.report.poller.ResettableStepRegistry;
import com.alipay.lookout.remote.step.LookoutRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiangfeng.xzc
 * @date 2018/8/16
 */
final class PollerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollerUtils.class);

    private PollerUtils() {
    }

    /**
     * 辅助方法, 通过HTTP暴露自身的metrics数据
     *
     * @param config
     * @param client
     * @return
     * @throws Exception
     */
    static MetricsHttpExporter exportHttp(LookoutConfig config, AbstractLookoutClient client) throws
        Exception {
        ResettableStepRegistry resettableStepRegistry = new ResettableStepRegistry(Clock.SYSTEM, config);

        final List<LookoutRegistry> lookoutRegistryList = new ArrayList<LookoutRegistry>();
        for (Registry r : client.getInnerCompositeRegistry().getRegistries()) {
            if (r instanceof LookoutRegistry) {
                lookoutRegistryList.add((LookoutRegistry) r);
            }
        }
        PollerController controller = new PollerController(resettableStepRegistry);
        controller.addListener(new Listener() {
            @Override
            public void onActive() {
                for (LookoutRegistry r : lookoutRegistryList) {
                    r.getMetricObserverComposite().setEnabled(false);
                }
            }

            @Override
            public void onIdle() {
                for (LookoutRegistry r : lookoutRegistryList) {
                    r.getMetricObserverComposite().setEnabled(false);
                }
            }
        });
        try {
            MetricsHttpExporter exporter = new MetricsHttpExporter(controller);
            exporter.start();
            return exporter;
        } catch (Exception e) {
            try {
                controller.close();
            } catch (Exception e2) {
                LOGGER.error("fail to close controller", e2);
            }
            throw e;
        }
    }
}
