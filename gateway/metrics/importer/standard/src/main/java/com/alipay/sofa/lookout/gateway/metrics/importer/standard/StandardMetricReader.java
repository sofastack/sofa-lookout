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
package com.alipay.sofa.lookout.gateway.metrics.importer.standard;

import com.alipay.sofa.lookout.gateway.core.common.LogUtils;
import com.alipay.sofa.lookout.gateway.core.prototype.reader.AbstractReader;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
public class StandardMetricReader extends AbstractReader<RawMetric, Metric> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardMetricReader.class);

    @Override
    public Stream<Metric> read(RawMetric rm) {
        // TODO 可以将解压的逻辑做到父类/通用类, 可能其他reader也会有解压的逻辑
        boolean snappy = rm.getHead().isSnappy();
        byte[] bytes = rm.getRawBody();
        if (snappy) {
            try {
                bytes = Snappy.uncompress(bytes);
            } catch (IOException e) {
                LOGGER.warn("snappy uncompress error {}", e.getMessage());
                return Stream.empty();
            }
        }
        String body = readUTF8(bytes);
        return Arrays.stream(StringUtils.split(body, '\t'))
                .flatMap(line -> {
                    // 一行的失败, 不要影响其他行, 所以这里要try/catch
                    try {
                        return StandardMetricConverter.parse(line);
                    } catch (Exception e) {
                        LogUtils.READER_LOGGER.warn("{}", line);
                        return Stream.empty();
                    }
                });
    }
}
