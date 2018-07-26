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
package com.alipay.lookout.remote.report.xflush;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.lookout.core.config.LookoutConfig;
import com.alipay.lookout.remote.step.PollerController;
import com.google.common.collect.Sets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * @author xiangfeng.xzc
 * @date 2018/7/17
 */
public class XFlushHttpExporter {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int DEFAULT_BACKLOG = 2;
    private static final int DEFAULT_PORT = 19399;
    private static final BasicThreadFactory THREAD_FACTORY = new BasicThreadFactory.Builder()
        .namingPattern("XFlushHttpExporter-ThreadPool-%d")
        .build();
    private final PollerController controller;
    private final int port;
    private final int backlog;
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private final int idleSeconds;
    private boolean active = false;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;
    private HttpServer httpServer;

    public XFlushHttpExporter(PollerController controller, LookoutConfig config) {
        this(controller, config, DEFAULT_PORT, DEFAULT_BACKLOG);
    }

    public XFlushHttpExporter(PollerController controller, LookoutConfig config, int port, int backlog) {
        this.controller = controller;
        this.port = port;
        this.backlog = backlog;
        idleSeconds = config.getInteger(LookoutConfig.XFLUSH_EXPORTER_IDLE_SECONDS, 60);
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1, THREAD_FACTORY,
            new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 启动exporter, 暴露底层的http端口
     *
     * @throws IOException
     */
    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), backlog);
        httpServer.createContext("/get", getHandler);
        // 测试用接口 清理掉数据
        httpServer.createContext("/clear", clearHandler);
        httpServer.start();
    }

    public synchronized void close() {
        if (httpServer != null) {
            httpServer.stop(5);
            httpServer = null;
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    /**
     * 用于get数据
     */
    private final HttpHandler getHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange)
            throws IOException {
            try {
                touchTimer();
                // 解析参数
                Set<Long> success = Collections.emptySet();
                long newStep = controller.getStep();
                int newSlotCount = controller.getSlotCount();

                for (NameValuePair nvp : parseParams(exchange)) {
                    String name = nvp.getName();
                    String value = nvp.getValue();
                    if ("step".equalsIgnoreCase(name)) {
                        newStep = Long.parseLong(value);
                    } else if ("slotCount".equalsIgnoreCase(name)) {
                        newSlotCount = Integer.parseInt(value);
                    } else if ("success".equalsIgnoreCase(name)) {
                        success = parseCursors(value);
                    }
                }

                Object data = controller.getNextData(success);

                JSONObject bodyEntity = new JSONObject();
                // 这里返回newStep给用户 表明我们已经接受了用户修改的step
                bodyEntity.put("step", newStep);
                bodyEntity.put("slotCount", newSlotCount);
                bodyEntity.put("data", data);
                sendResponse(exchange, bodyEntity);

                controller.update(newStep, newSlotCount);

                // if (oldRate != newStep || oldSlotCount != newSlotCount) {
                // }
            } finally {
                exchange.close();
            }
        }
    };

    /**
     * 刷新一下定时器
     */
    private synchronized void touchTimer() {
        // 说明从 idle 状态 转成 active 状态
        if (!active) {
            active = true;
            triggerActive();
        }

        // 取消旧的计时器
        ScheduledFuture<?> oldFuture = this.scheduledFuture;
        if (oldFuture != null && !oldFuture.isDone()) {
            oldFuture.cancel(true);
        }

        // 5分钟后进入idle状态
        this.scheduledFuture = scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (XFlushHttpExporter.this) {
                    active = false;
                    triggerIdle();
                }
            }
        }, idleSeconds, TimeUnit.SECONDS);
    }

    private void triggerIdle() {
        for (Listener listener : listeners) {
            listener.onIdle();
        }
    }

    private void triggerActive() {
        for (Listener listener : listeners) {
            listener.onActive();
        }
    }

    private final HttpHandler clearHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange)
            throws IOException {
            try {
                controller.clear();
                exchange.sendResponseHeaders(204, -1);
            } finally {
                exchange.close();
            }
        }
    };

    private static void sendResponse(HttpExchange exchange, Object bodyEntity) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = new GZIPOutputStream(exchange.getResponseBody());
        try {
            JSON.writeJSONString(os, UTF8, bodyEntity);
        } finally {
            os.close();
        }
    }

    private static Set<Long> parseCursors(String str) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptySet();
        }
        String[] ss = StringUtils.split(str, ',');
        Set<Long> set = Sets.newHashSetWithExpectedSize(ss.length);
        for (String s : ss) {
            set.add(Long.parseLong(s));
        }
        return set;
    }

    /**
     * 解析参数
     *
     * @param exchange
     * @return
     */
    private static List<NameValuePair> parseParams(HttpExchange exchange) {
        return new URIBuilder(exchange.getRequestURI()).getQueryParams();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }
}
