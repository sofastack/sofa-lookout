package com.alipay.lookout.starter.support.reg;

import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.step.LookoutRegistry;
import com.alipay.lookout.report.LogObserver;
import com.alipay.lookout.report.MetricObserver;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kevin.luy@alipay.com on 2018/5/23.
 */
public class LookoutServerRegistryFactoryTest {

    @Test
    public void testLookoutServerRegistryFactory() {
        MetricObserver<LookoutMeasurement> logObserver = new LogObserver();
        LookoutServerRegistryFactory factory = new LookoutServerRegistryFactory(Lists.newArrayList(logObserver));
        LookoutConfig config = new LookoutConfig();
        LookoutRegistry r = factory.get(config);
        Assert.assertEquals(2, r.getMetricObservers().size());
    }
}
