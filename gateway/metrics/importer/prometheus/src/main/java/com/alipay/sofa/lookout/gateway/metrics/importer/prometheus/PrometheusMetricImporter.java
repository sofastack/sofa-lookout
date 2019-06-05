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

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.lookout.gateway.core.scrape.JobState;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeJob;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeManager;
import com.alipay.sofa.lookout.gateway.core.scrape.ScrapeResult;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricImporterUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.importer.AbstractWebfluxImporter;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.SourceType;
import com.google.common.collect.Sets;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alipay.sofa.lookout.gateway.metrics.pipeline.common.MetricImporterUtils.resolveExtraTagsFromURI;

/**
 * @author: kevin.luy@antfin.com
 * @date 2018/11/15
 */
public class PrometheusMetricImporter extends AbstractWebfluxImporter {
    ScrapeManager                    scrapeMananger;

    /**
     * TODO 临时兼容性代码 于双十二之后的发布版本删除
     */
    private static final Set<String> WHITE_LIST = Sets.newHashSet("");

    public PrometheusMetricImporter() {
        super("prometheus");
    }

    @Override
    protected Mono<ServerResponse> doHandle(ServerRequest request, RawMetric rm) {
        // TODO 能否将url tags的提取放到父类去做?
        // prometheus以数据到达的时间为时间戳
        Map<String, String> extraTags = rm.getExtraTags();
        resolveExtraTagsFromURI("/prom/metrics/", request.uri().getPath(), extraTags);
        String job = extraTags.get("job");
        if (job != null && !WHITE_LIST.contains(job)) {
            MetricImporterUtils.validExtraTags(extraTags);
        }
        rm.setSourceType(SourceType.PROMETHEUS);
        rm.setPushMode(true);
        super.fire(rm);
        return success();
    }

    public void pull(ScrapeResult<byte[]> scrapeResult) {
        RawMetric rm = new RawMetric();
        rm.setVersion(VERSION);
        // 先假设以当前时间作为metric时间
        rm.setTimestamp(System.currentTimeMillis());
        //        RawMetricHead head = rm.getHead();
        //        head.setToken(LookoutTokenResolveUtils.getLookoutToken(request.headers()));
        //        head.setDebugId(WebfluxUtils.getHeaderValue(request, "X-Debug-Id"));
        //        head.setClientIp(inputMessage.getRemoteAddress().getAddress().getHostAddress());
        //        size.record(metricData.length);
        rm.setRawBody(scrapeResult.getBody());
        Map<String, String> extraTags = rm.getExtraTags();
        extraTags.put("job", scrapeResult.getConfig().getJobName());
        extraTags.put("app", scrapeResult.getConfig().getJobName());
        extraTags.put("instance", scrapeResult.getHeaders().get("target").get(0));
        //影响到时间对齐
        extraTags.put("step", String.valueOf(scrapeResult.getConfig().getScrapeInterval() / 1000));
        //        resolveExtraTagsFromURI("/prom/metrics/", request.uri().getPath(), extraTags);
        //        String job = extraTags.get("job");
        //        if (job != null && !WHITE_LIST.contains(job)) {
        //            MetricImporterUtils.validExtraTags(extraTags);
        //        }
        rm.getHead().setClientIp(extraTags.get("instance"));
        rm.setSourceType(SourceType.PROMETHEUS);
        rm.setPushMode(false);
        super.fire(rm);
    }

    public void setScrapeMananger(ScrapeManager scrapeMananger) {
        this.scrapeMananger = scrapeMananger;
    }

    public Mono<ServerResponse> fetchTargets(ServerRequest serverRequest) {
        //getRunningjobs
        Map<String, ScrapeJob> runningJobs = scrapeMananger.getJobProcessor().getRunnings();
        Map<String, List<JobState>> states = new HashMap<>();
        for (ScrapeJob job : runningJobs.values()) {
            states.put(job.getJobName(), job.getStates());
        }
        ServerResponse.BodyBuilder b = ServerResponse.ok().contentType(MediaType.APPLICATION_JSON);
        return b.syncBody(JSON.toJSONString(states));
    }
}
