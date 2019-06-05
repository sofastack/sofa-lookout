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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.queue;

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.gateway.core.prototype.queue.BasePersistQueueProcessor;
import com.alipay.sofa.lookout.gateway.core.prototype.queue.Serializer;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;

/**
 * 针对metrics的队列处理器实现, 将输入的数据放到持久的化队列里, 然后会有若干个线程不停的持久化轮询队列, 将轮询到的数据作为该处理器的输出
 *
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public class MetricsPersistQueueProcessor extends BasePersistQueueProcessor<RawMetric> {
    public static final Serializer<RawMetric> JSON_SERIALIZER = new Serializer<RawMetric>() {
                                                                  @Override
                                                                  public byte[] serialize(RawMetric rawMetric) {
                                                                      return JSON
                                                                          .toJSONBytes(rawMetric);
                                                                  }

                                                                  @Override
                                                                  public RawMetric deserialize(byte[] bytes) {
                                                                      return JSON.parseObject(
                                                                          bytes, RawMetric.class);
                                                                  }
                                                              };

    public MetricsPersistQueueProcessor(int threads, String dir, String queueName, Registry registry) {
        super(threads, dir, queueName, registry, JSON_SERIALIZER);
    }

    /**
     * 我们要定制一些入队规则 所以重写这个方法
     *
     * @param rawMetric
     */
    @Override
    public void onInput(RawMetric rawMetric) {
        if (rawMetric.getHead().getDebugId() != null) {
            // debug 模式直接进入output 保证整条链路是串行的
            super.onOutput(rawMetric);
            //暂时无论push or pull都进入本地队列先；
        } else {
            super.onInput(rawMetric);
        }
    }
}
