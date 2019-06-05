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
package com.alipay.sofa.lookout.gateway.metrics.starter;

import com.alipay.sofa.lookout.gateway.core.prototype.reader.Reader;
import com.alipay.sofa.lookout.gateway.metrics.importer.metricbeat.MetricbeatMetricReader;
import com.alipay.sofa.lookout.gateway.metrics.importer.opentsdb.OpentsdbMetricReader;
import com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.PrometheusMetricReader;
import com.alipay.sofa.lookout.gateway.metrics.importer.standard.StandardMetricReader;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricImporterUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.Metric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.reader.ReaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * TODO reader 没有太多逻辑的话就做成静态的吧 TODO 能否自动注册?
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class DefaultReaderManager implements ReaderManager {
    private static final Logger          LOGGER     = LoggerFactory
                                                        .getLogger(DefaultReaderManager.class);

    private final StandardMetricReader   standard   = new StandardMetricReader();
    private final PrometheusMetricReader prometheus = new PrometheusMetricReader();
    private final OpentsdbMetricReader   opentsdb   = new OpentsdbMetricReader();
    private final MetricbeatMetricReader metricbeat = new MetricbeatMetricReader();

    private Reader<RawMetric, Metric> getReader(SourceType sourceType) {
        switch (sourceType) {
            case STANDARD:
                return standard;
            case PROMETHEUS:
                return prometheus;
            case OPENTSDB:
                return opentsdb;
            case METRICBEAT:
                return metricbeat;
            default:
                return null;
        }
    }

    @Override
    public Stream<Metric> read(RawMetric rm) {
        SourceType sourceType = rm.getSourceType();
        Reader<RawMetric, Metric> r = getReader(sourceType);
        if (r != null) {
            try {
                Stream<Metric> stream = r.read(rm);

                // 是所以将这几个增强逻辑放在这里这是因为 reader 是连接 前后两种泛型的地方 RawMetric Metric
                // 出了这个地方就没有办法同时获取到两者了

                // 增强1: 如果tags不携带ip, 就将上报metric的客户端ip附在tags上
                String clientIp = rm.getHead().getClientIp();
                if (clientIp != null) {
                    stream = stream.peek(m -> m.getTags().putIfAbsent("ip", clientIp));
                }

                // 增强2: 非标准上传的metrics对齐时间戳
                stream = stream.peek(m -> fixTimestamp(rm, m));

                // 增强3: 传递debugId
                String debugId = rm.getHead().getDebugId();
                if (debugId != null) {
                    stream = stream.peek(m -> m.setDebugId(debugId));
                }

                // 增强4: 合并tags
                stream = stream.peek(m -> MetricImporterUtils.mergeWithExtraTags(m, rm.getExtraTags()));

                return stream;
            } catch (Exception e) {
                // 由于stream的op是lazy的, 因此同上上述操作不会抛异常, 而是等待具体的对象被处理的时候才会抛异常
                LOGGER.warn("读metric时失败", e);
                return Stream.empty();
            }
        } else {
            LOGGER.warn("找不到 {} 的reader", sourceType);
            return Stream.empty();
        }
    }

    /**
     * 对齐时间戳
     *
     * @param rm
     * @param m
     */
    private void fixTimestamp(RawMetric rm, Metric m) {
        // standard不需要对齐
        // TODO standard 只能表示使用自有协议上传, 但有可能是非lookout-SDK自己拼凑自有协议request上传的!
        if (rm.getSourceType() == SourceType.STANDARD) {
            return;
        }
        String stepStr = rm.getExtraTags().remove("step");
        // 默认对齐到10s
        int stepSeconds = 10;
        if (stepStr != null) {
            int stepSeconds2 = Integer.parseInt(stepStr);
            // -1表示不对齐, 其他的值只有>0的才是合法的
            if (stepSeconds2 == -1 || stepSeconds2 > 0) {
                stepSeconds = stepSeconds2;
            }
            // 小于等于0，默认不做对齐
        }
        if (stepSeconds > 0) {
            long stepMills = stepSeconds * 1000L;
            // 对齐时间
            m.setTimestamp(m.getTimestamp() / stepMills * stepMills);
        }
    }
}
