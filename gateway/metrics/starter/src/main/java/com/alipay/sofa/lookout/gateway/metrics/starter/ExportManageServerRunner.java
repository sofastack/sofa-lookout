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

import com.alipay.sofa.lookout.gateway.core.common.RefuseRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

/**
 * gateway在6200端口暴露一个管理用的http server, 可以对程序下达操作指令
 *
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
@SuppressWarnings("CodeBlock2Expr")
public class ExportManageServerRunner implements ApplicationRunner {
    private static final Logger    LOGGER = LoggerFactory.getLogger(ExportManageServerRunner.class);

    @Value("${server.port:7200}")
    protected int                  serverPort;

    @Autowired
    protected RefuseRequestService refuseRequestService;

    /**
     * 暴露6200管理端口
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        RouterFunction manageRouterFunction = RouterFunctions
                .route(RequestPredicates.GET("/ok"), req -> {
                    return ServerResponse.ok()
                            .syncBody("online");
                })
                .andRoute(RequestPredicates.GET("/cmd/{line}"), request -> {
                    String pathVar = request.pathVariable("line");
                    try {
                        if ("down".equals(pathVar)) {
                            refuseRequestService.setRefuseRequest(true);
                        } else if ("up".equals(pathVar)) {
                            refuseRequestService.setRefuseRequest(false);
                        }
                        return ServerResponse.ok().body(Mono.just("ok"), String.class);
                    } catch (Throwable e) {
                        LOGGER.error("{} request err!", pathVar, e);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                });

        HttpHandler handler = RouterFunctions.toHttpHandler(manageRouterFunction);
        int managePort = serverPort - 1000;

        ReactorHttpHandlerAdapter inboundAdapter = new ReactorHttpHandlerAdapter(handler);

        // manage port
        HttpServer.create().port(managePort).handle(inboundAdapter).bind();
        // HttpServer.create(managePort).newHandler(inboundAdapter).block();

        LOGGER.info("management services run on port:{}", managePort);
    }
}
