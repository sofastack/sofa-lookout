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
package com.alipay.lookout.remote.report.support.http;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import com.alipay.lookout.remote.report.AddressService;
import com.alipay.lookout.remote.report.DefaultAddressService;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-20 20:50
 **/
public class ReportConfigUtilTest {

    List<LookoutMeasurement> measurements;

    @Before
    public void init() {
        Registry registry = new DefaultRegistry();
        measurements = new ArrayList<LookoutMeasurement>();
        int i = 1;
        Id id = registry.createId("jvm.memory.free");
        LookoutMeasurement measurement = new LookoutMeasurement(new Date(), id);
        measurement.addTag("k" + i, "v" + i);
        measurement.addTag("aa" + i, "bb" + i);
        measurement.put("xxx" + i, i);
        measurement.put("yyy" + i, i);
        measurements.add(measurement);

        id = registry.createId("rpc.consumer.aa");
        measurement = new LookoutMeasurement(new Date(), id);
        measurement.addTag("k2", "v2");
        measurement.addTag("aa", "bb");
        measurement.put("xxx" + i, i);
        measurements.add(measurement);

        id = registry.createId("jvm.memory.free");
        measurement = new LookoutMeasurement(new Date(), id);
        measurement.addTag("k1", "v1");
        measurement.addTag("k2", "v2");
        measurement.put("xxx" + i, i);
        measurements.add(measurement);

        id = registry.createId("xx.yy.zz");
        measurement = new LookoutMeasurement(new Date(), id);
        measurement.addTag("k1", "v1");
        measurement.addTag("k2", "v2");
        measurement.put("xxx" + i, i);
        measurements.add(measurement);

        id = registry.createId("xx.yy.zz");
        measurement = new LookoutMeasurement(new Date(), id);
        measurement.addTag("xx", "v1");
        measurement.addTag("sf", "v2");
        measurement.put("xxx" + i, i);
        measurements.add(measurement);
    }

    @Test
    public void testRefreshReportConfig() throws UnsupportedEncodingException {
        ReportConfigUtil reportConfigUtil = new ReportConfigUtil();
        reportConfigUtil
            .getConfigResultConsumer()
            .consume(
                new StringEntity(
                    "{\"id\":\"xxx\",\"config\":{\"name_pre_wl\":\"jvm.memory,rpc.consumer\",\"tag_wl\":\"k1=v1,k2=v2\"}}"));
        Assert.assertEquals(5, measurements.size());
        List<LookoutMeasurement> measurementList = reportConfigUtil.filter(measurements);
        Assert.assertEquals(4, measurementList.size());
    }

    @Test
    public void testRefreshReportConfigWithBlankValue() throws UnsupportedEncodingException {
        ReportConfigUtil reportConfigUtil = new ReportConfigUtil();
        reportConfigUtil.getConfigResultConsumer().consume(
            new StringEntity("{\"id\":\"xxx\",\"config\":{\"name_pre_wl\":\"\",\"tag_wl\":\"\"}}"));
        Assert.assertEquals(5, measurements.size());
        List<LookoutMeasurement> measurementList = reportConfigUtil.filter(measurements);
        Assert.assertEquals(0, measurementList.size());
    }

    @Test
    public void testRefreshReportConfigWithoutConfigs() throws UnsupportedEncodingException {
        ReportConfigUtil reportConfigUtil = new ReportConfigUtil();
        reportConfigUtil.getConfigResultConsumer().consume(
            new StringEntity("{\"id\":\"xxx\",\"config\":{}}"));
        Assert.assertEquals(5, measurements.size());
        List<LookoutMeasurement> measurementList = reportConfigUtil.filter(measurements);
        Assert.assertEquals(5, measurementList.size());
    }

    @Test
    public void testRefreshReportConfigOnlyMetricName() throws UnsupportedEncodingException {
        ReportConfigUtil reportConfigUtil = new ReportConfigUtil();
        reportConfigUtil.getConfigResultConsumer().consume(
            new StringEntity(
                "{\"id\":\"xxx\",\"config\":{\"name_pre_wl\":\"jvm.memory,rpc.consumer\"}}"));
        Assert.assertEquals(5, measurements.size());
        List<LookoutMeasurement> measurementList = reportConfigUtil.filter(measurements);
        Assert.assertEquals(3, measurementList.size());
    }

    @Test
    public void testRefreshReportConfigOnlyTags() throws UnsupportedEncodingException {
        ReportConfigUtil reportConfigUtil = new ReportConfigUtil();
        reportConfigUtil.getConfigResultConsumer().consume(
            new StringEntity("{\"id\":\"xxx\",\"config\":{\"tag_wl\":\"k1=v1,k2=v2\"}}"));
        Assert.assertEquals(5, measurements.size());
        List<LookoutMeasurement> measurementList = reportConfigUtil.filter(measurements);
        Assert.assertEquals(4, measurementList.size());
    }

    @Test
    public void testFilterMeasures() {
        AddressService addressService = new DefaultAddressService();
        DefaultHttpRequestProcessor p = new DefaultHttpRequestProcessor(addressService,
            new LookoutConfig());
        List<LookoutMeasurement> measurementList = p.filter(measurements);
        Assert.assertEquals(measurements.size(), measurementList.size());
    }

}
