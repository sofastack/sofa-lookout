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
package com.alipay.lookout.api;

import java.util.Map;

/**
 * 抽象实现, 实现了id的构建 和 clock 的支持
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public abstract class MetricRegistry implements Registry {
    private boolean     propagateWarnings = true;
    private final Clock clock;

    public MetricRegistry(Clock clock) {
        this.clock = clock;
    }

    /**
     * use other extension metrics,such as: jvm,vm,middleWares...
     */
    public abstract void registerExtendedMetrics();

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public final Id createId(String name) {
        return new DefaultId(name);
    }

    @Override
    public final Id createId(String name, Iterable<Tag> tags) {
        return new DefaultId(name, TagSet.create(tags));
    }

    /**
     * Creates an identifier for a meter.
     *
     * @param name Description of the measurement that is being collected.
     * @param tags Other dimensions that can be used to classify the measurement.
     * @return Identifier for a meter.
     */
    @Override
    public final Id createId(String name, Map<String, String> tags) {
        return createId(name).withTags(tags);
    }

    @Override
    public void propagate(String msg, Throwable t) {
        // LookoutLoggerFactory.getLogger(getClass()).warn(msg, t);
        if (propagateWarnings) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    protected void propagate(Throwable t) {
        propagate(t.getMessage(), t);
    }

}
