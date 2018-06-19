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
package com.alipay.lookout.starter.support.converter;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Indicator;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.common.LookoutConstants;
import com.alipay.lookout.jvm.LookoutIdNameConstants;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.utils.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.*;

/**
 * IndicatorConvert
 *
 * @author yangguanchao
 * @since 2018/06/05
 */
public class IndicatorConvert {

    private static final Logger   logger                       = LookoutLoggerFactory
                                                                   .getLogger(IndicatorConvert.class);

    /***
     * ignore return {@link Metric} key words composite
     */
    private static final String[] IGNORED_METRIC_NAME_PREFIXES = new String[] {
            LookoutIdNameConstants.JVM_SYSTEM_PROP_NAME,
            LookoutIdNameConstants.JVM_SYSTEM_PROP_NAME       };

    private static boolean isIgnoredMetrics(String namePrefix) {
        if (StringUtils.isBlank(namePrefix)) {
            return false;
        }
        for (String ignoredMetricNamePrefix : IGNORED_METRIC_NAME_PREFIXES) {
            if (namePrefix.contains(ignoredMetricNamePrefix)) {
                return true;
            }
        }
        return false;
    }

    /***
     * Convert form lookout  {@link Indicator} to Actuator {@link Metric} and also exposed to browser
     * @param indicator Lookout
     * @return Actuator Metrics
     */
    public static List<Metric> convertFromIndicator(Indicator indicator) {
        if (indicator == null) {
            return null;
        }
        List<Metric> indicatorMetricList = new LinkedList<Metric>();
        Id id = indicator.id();
        Date date = new Date(indicator.getTimestamp());
        String namePrefix = "";
        if (id != null) {
            namePrefix = CommonUtil.toMetricName(id);
        }
        //ignore collection
        if (isIgnoredMetrics(namePrefix)) {
            return indicatorMetricList;
        }

        try {
            Collection<Measurement> measurements = indicator.measurements();
            for (Measurement measurement : measurements) {
                String name = measurement.name();
                Object measureValue = measurement.value();
                if (measureValue instanceof Number) {
                    Number valueNumber = (Number) measureValue;
                    Metric<Number> metric = new Metric<Number>(namePrefix + LookoutConstants.DOT
                                                               + name, valueNumber, date);
                    indicatorMetricList.add(metric);
                } else if (measureValue instanceof Map) {
                    Map<String, Object> valueMap = (Map<String, Object>) measureValue;
                    for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                        String keyName = entry.getKey();
                        Object value = entry.getValue();
                        if (value == null) {
                            continue;
                        }
                        if (value instanceof Number) {
                            Number valueMapNumber = (Number) value;
                            Metric<Number> metric = new Metric<Number>(namePrefix
                                                                       + LookoutConstants.DOT
                                                                       + name
                                                                       + LookoutConstants.DOT
                                                                       + keyName, valueMapNumber,
                                date);
                            indicatorMetricList.add(metric);
                        } else {
                            //ignore collection
                            logger.debug("Lookout value is not instance of Number. Value type is ["
                                         + value.getClass() + "].Ignored Lookout prefix = " + "["
                                         + namePrefix + LookoutConstants.DOT + name
                                         + "] Measurement = [" + entry.toString() + "]");
                        }
                    }
                } else {
                    //ignore collection
                    logger.debug("Lookout value is not instance of Number. Value type is ["
                                 + measureValue.getClass() + "].Ignored Lookout prefix = " + "["
                                 + namePrefix + "] Measurement = [" + measurement.toString() + "]");
                }
            }
        } catch (Exception exception) {
            logger.error("Indicator converted from Lookout Exception!", exception);
        }
        return indicatorMetricList;
    }
}
