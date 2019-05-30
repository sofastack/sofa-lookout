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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.scrape;

import com.alipay.sofa.lookout.gateway.core.prototype.importer.AbstractImporter;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeManager;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.google.common.base.Preconditions;

/**
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2019/1/15
 */
public class ScrapeImporter extends AbstractImporter<RawMetric> {

    private final ScrapeManager scrapeManager;

    public ScrapeImporter(ScrapeManager scrapeManager) {
        super(scrapeManager.getClass().getSimpleName());
        this.scrapeManager = Preconditions.checkNotNull(scrapeManager);
    }

    @Override
    protected void doStart() {
        // 启动定时任务 定期拉取数据 并且调用 super.fire() 发射数据
    }
}
