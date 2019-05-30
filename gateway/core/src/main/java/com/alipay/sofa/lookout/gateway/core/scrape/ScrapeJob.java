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
package com.alipay.sofa.lookout.gateway.core.scrape;

import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * @author: kevin.luy@alibaba-inc.com
 * @create: 2019-01-03 18:28
 **/
public abstract class ScrapeJob<T extends ScrapeConfig> implements Runnable {
    protected Logger log = LoggerFactory.getLogger(ScrapeJob.class);
    private Future future;
    private T scrapeConfig;
    private List<JobState> states = new ArrayList<>();

    public ScrapeJob(T scrapeConfig) {
        this.scrapeConfig = scrapeConfig;
    }

    public void stop() {
        if (!future.isCancelled()) {
            future.cancel(true);
        }
    }

    public List<JobState> getStates() {
        return states;
    }

    public T getConfig() {
        return scrapeConfig;
    }

    public void setConfig(T scrapeConfig) {
        this.scrapeConfig = scrapeConfig;
    }

    @Override
    public void run() {
        List<JobState> jobStates = new ArrayList<>();
        try {
            scrape(jobStates);
        } catch (Throwable e) {
            log.warn("scrape fail!" + e.getMessage());
        } finally {
            states = jobStates;
        }
    }

    protected abstract void scrape(List<JobState> jobStates);

    public void schedule(ScheduledExecutorService scheduledExecutorService) {
        future = scheduledExecutorService.scheduleAtFixedRate(this, 0, getConfig()
                .getScrapeInterval(), TimeUnit.MILLISECONDS);
    }

    public boolean isRunning() {
        return !future.isCancelled();
    }

    public String getJobName() {
        return getConfig().getJobName();
    }
}
