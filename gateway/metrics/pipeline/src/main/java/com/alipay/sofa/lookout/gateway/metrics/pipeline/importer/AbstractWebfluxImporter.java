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
package com.alipay.sofa.lookout.gateway.metrics.pipeline.importer;

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.DistributionSummary;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.sofa.lookout.gateway.core.common.RefuseRequestService;
import com.alipay.sofa.lookout.gateway.core.common.WebfluxUtils;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.parser.FilterException;
import com.alipay.sofa.lookout.gateway.core.prototype.importer.AbstractImporter;
import com.alipay.sofa.lookout.gateway.core.token.LookoutTokenResolveUtils;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetric;
import com.alipay.sofa.lookout.gateway.metrics.pipeline.model.RawMetricHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于webflux的importer通常要做成spring的bean, 因此这里就直接使用spring的注解了, 否则通常这些参数通常是通过构造函数传过来 基于webflux实现的导入器, 等待客户端推数据过来
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/15
 */
public abstract class AbstractWebfluxImporter extends AbstractImporter<RawMetric> {
    /**
     * 如果你认为对 RawMetric 的修改会产生不兼容, 那么就将这个字段++, 这样所有读出来的旧数据丢会被丢弃 如果能在程序启动的时候删除所有旧的持久化队列也行
     */
    public static final int       VERSION = 1;
    protected final Logger        LOGGER  = LoggerFactory.getLogger(getClass());
    protected Counter             counter;
    protected DistributionSummary size;

    @Autowired
    private Registry              registry;
    @Autowired
    private RefuseRequestService  refuseRequestService;

    public AbstractWebfluxImporter(String name) {
        super(name);
    }

    @PostConstruct
    public void init() {
        Map<String, String> tags = new HashMap<>();
        tags.put("importer", name);
        tags.put("type", "metric");
        Id id = registry.createId("importer.stats", tags);
        MixinMetric mm = registry.mixinMetric(id);
        counter = mm.counter("count");
        size = mm.distributionSummary("size");
    }

    /**
     * 初始化出一个新的 RawMetric
     *
     * @param request
     * @return
     */
    protected RawMetric initRawMetric(ServerRequest request) {
        RawMetric rm = new RawMetric();
        rm.setVersion(VERSION);
        // 先假设以当前时间作为metric时间
        rm.setTimestamp(System.currentTimeMillis());
        RawMetricHead head = rm.getHead();
        head.setToken(LookoutTokenResolveUtils.getLookoutToken(request.headers()));
        head.setDebugId(WebfluxUtils.getHeaderValue(request, "X-Debug-Id"));
        return rm;
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        if (refuseRequestService.isRefuseRequest()) {
            return error("lookout gateway is in refuse requests mode");
        }
        counter.inc();

        RawMetric rm = initRawMetric(request);

        // 目前获取客户端IP可以使用如下的方式 关键是要获取到 ServerHttpRequest 对象
        // extractor 可否重用?
        BodyExtractor<Mono<byte[]>, ReactiveHttpInputMessage> extractor = BodyExtractors.toMono(byte[].class);
        return request.body((inputMessage, context) -> {
            RawMetricHead head = rm.getHead();
            head.setClientIp(inputMessage.getRemoteAddress().getAddress().getHostAddress());
            return extractor.extract(inputMessage, context);
        }).flatMap(body -> {
            size.record(body.length);
            rm.setRawBody(body);
            return doHandle(request, rm);
        }).onErrorResume(error -> {
            if (error instanceof FilterException) {
                FilterException fe = (FilterException) error;
                return error(fe.getResult().getMsg());
            } else {
                return error(error.getMessage());
            }
        });
    }

    /**
     * 返回一个错误响应
     *
     * @param msg
     * @return
     */
    protected Mono<ServerResponse> error(String msg) {
        ServerResponse.BodyBuilder b = ServerResponse.badRequest();
        b.header("Err", msg);
        return b.syncBody(msg);
    }

    protected Mono<ServerResponse> success() {
        return ServerResponse.accepted().build();
    }

    /**
     * <p>由子类实现,进行进一步处理, 注意有一些公共字段已经填充好了, 类无需再去填充, 比如body之类</p>
     * 子类可以不捕获异常, 因为父类已经统一try/catch了
     *
     * @param request
     * @param rm
     * @return
     */
    protected abstract Mono<ServerResponse> doHandle(ServerRequest request, RawMetric rm);
}
