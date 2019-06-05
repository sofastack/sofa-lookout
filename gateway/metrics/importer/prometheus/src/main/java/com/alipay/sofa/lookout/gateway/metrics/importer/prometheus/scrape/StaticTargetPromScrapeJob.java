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

import com.alipay.sofa.lookout.gateway.core.common.LogUtils;
import com.alipay.sofa.lookout.gateway.core.scrape.JobState;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeJob;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeResult;
import com.google.common.collect.Lists;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-04 10:40
 **/
public class StaticTargetPromScrapeJob extends ScrapeJob<StaticScrapeConfig> {

    private final OkHttpClient             client;
    private Consumer<ScrapeResult<byte[]>> consumer;

    public StaticTargetPromScrapeJob(StaticScrapeConfig scrapeConfig) {
        this(scrapeConfig, null);
    }

    public StaticTargetPromScrapeJob(StaticScrapeConfig scrapeConfig,
                                     Consumer<ScrapeResult<byte[]>> consumer) {
        super(scrapeConfig);
        client = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(scrapeConfig.getScrapeTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(scrapeConfig.getScrapeTimeout(), TimeUnit.MILLISECONDS).build();
        this.consumer = consumer;

    }

    @Override
    protected void scrape(List<JobState> jobStates) {
        for (StaticScrapeConfig.StaticConfigItem item : getConfig().getStaticConfigItemList()) {
            for (String target : item.getTargets()) {
                JobState jobState = new JobState();
                jobStates.add(jobState);
                jobState.setLastScrapedTime(Instant.now());
                long start = System.currentTimeMillis();
                try {
                    // String targetUrl = getConfig().getSchema() + "://" + target + getConfig().getMetricsPath();
                    UriBuilder uriBuilder = UriComponentsBuilder.newInstance().scheme(getConfig().getSchema()).host(target).path(getConfig().getMetricsPath());

                    //add params
                    if (getConfig().getParams() != null) {
                        for (Map.Entry<String, List<String>> entry : getConfig().getParams().entrySet()) {
                            uriBuilder.queryParam(entry.getKey(), entry.getValue().toArray());

                        }
                    }
                    String targetUrl = ((UriComponentsBuilder) uriBuilder).build().toUriString();
                    Request request = new Request.Builder().url(targetUrl).header("User-Agent", "lookout-gateway").build();
                    jobState.setEndpoint(targetUrl);
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    Map<String, List<String>> headers = response.headers().toMultimap();
                    headers.put("target", Lists.newArrayList(target));
                    if (consumer != null)
                        consumer.accept(new ScrapeResult<>(response.body().bytes(), headers, getConfig()));
                    jobState.setSuccessful(true);
                } catch (Throwable e) {
                    jobState.setSuccessful(false);
                    jobState.setError(e.getMessage());
                    log.warn("scrape fail!" + e.getMessage());
                } finally {
                    jobState.setDuration(Duration.ofMillis(System.currentTimeMillis() - start));
                    //最近一次拉取的状态记录
                    LogUtils.SCRAPE_DIGEST_LOGGER
                            .info("|{}|{}|{}|{}|", jobState.getEndpoint(), getJobName(), jobState.isSuccessful() ? "T" : "F", jobState
                                    .getDuration().toMillis());
                }

            }

        }
    }
}
