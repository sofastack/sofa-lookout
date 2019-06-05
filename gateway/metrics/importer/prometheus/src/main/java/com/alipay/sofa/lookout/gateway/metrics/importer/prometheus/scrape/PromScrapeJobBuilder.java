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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.scrape;

import com.alipay.sofa.lookout.gateway.core.scrape.JobBuilder;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeJob;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeResult;
import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;

import java.util.function.Consumer;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-04 11:47
 **/
public class PromScrapeJobBuilder implements JobBuilder {
    Consumer<ScrapeResult<byte[]>> consumer;

    public PromScrapeJobBuilder() {
    }

    public PromScrapeJobBuilder(Consumer<ScrapeResult<byte[]>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public ScrapeJob build(ScrapeConfig config) {
        if (config instanceof StaticScrapeConfig) {
            return new StaticTargetPromScrapeJob((StaticScrapeConfig) config, consumer);
        }
        return null;
    }
}
