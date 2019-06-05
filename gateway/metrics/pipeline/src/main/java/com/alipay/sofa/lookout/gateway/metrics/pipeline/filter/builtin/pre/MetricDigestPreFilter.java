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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.filter.builtin.pre;

import com.alipay.sofa.lookout.gateway.core.prototype.filter.AbstractFilter;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.utils.DigestLogUtil;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetricHead;

import java.util.Map;

/**
 * 打印digest日志
 *
 * @author: kevin.luy@antfin.com
 * @date 2018/11/27
 */
public class MetricDigestPreFilter extends AbstractFilter<RawMetric> {

    @Override
    public String type() {
        return "metric";
    }

    @Override
    public Filter.FilterResult test(RawMetric rm, Map<String, ?> filterContext) {
        DigestLogUtil.AccessRecord r = new DigestLogUtil.AccessRecord();
        RawMetricHead head = rm.getHead();
        r.setType("metric");
        r.setPriority(head.getStandardPriority());
        r.setSrc(rm.getSourceType().name());
        r.setClientIp(head.getClientIp());
        r.setClientApp(head.getStandardAppName());
        r.setBodySize(rm.getRawBody().length);
        r.setToken(head.getToken());
        DigestLogUtil.print(r);
        return SUCCESS;
    }
}
