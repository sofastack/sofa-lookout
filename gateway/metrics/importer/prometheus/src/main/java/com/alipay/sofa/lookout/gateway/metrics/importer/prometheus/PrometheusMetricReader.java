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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus;

import com.alipay.sofa.lookout.gateway.core.prototype.reader.AbstractReader;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
public class PrometheusMetricReader extends AbstractReader<RawMetric, Metric> {
    @Override
    public Stream<Metric> read(RawMetric rm) {
        String body = readUTF8(rm.getRawBody());
        long timestamp = rm.getTimestamp();
        return Arrays.stream(StringUtils.split(body, '\n'))
                .filter(line -> !line.startsWith("#"))
                .map(line -> {
                    Metric m = PrometheusConverter.convertToModel(line);
                    if (m == null) {
                        return null;
                    }
                    m.setTimestamp(timestamp);
                    return m;
                });
    }
}
