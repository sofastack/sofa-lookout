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
package com.alipay.lookout.reg.prometheus.exporter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class ExporterServer {
    final HttpServer httpServer;

    public ExporterServer(int port) {
        final ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100), getNamedThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 2);
            httpServer.setExecutor(singleThreadPool);
        } catch (IOException e) {
            throw new RuntimeException("prometheus exporter server create err!", e);
        }
    }

    private static ThreadFactory getNamedThreadFactory() {
        //使用guava包中工具类；
        return new ThreadFactoryBuilder().setNameFormat("prometheus-exporter-pool-%d").build();
    }

    public void addMetricsQueryHandler(HttpHandler httpHandler) {
        httpServer.createContext("/metrics", httpHandler);
    }

    public void start() {
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                byte[] respContents = "<html><head><title>Lookout Client Exporter</title></head><body><h1>Lookout Client Exporter</h1><p><a href=\"/metrics\">Metrics</a></p></body></html>"
                    .getBytes("UTF-8");
                httpExchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(200, respContents.length);
                httpExchange.getResponseBody().write(respContents);
                httpExchange.close();
            }
        });
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
