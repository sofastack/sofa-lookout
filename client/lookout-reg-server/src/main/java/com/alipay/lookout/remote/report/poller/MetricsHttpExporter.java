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
package com.alipay.lookout.remote.report.poller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.lookout.common.log.LookoutLoggerFactory;
import com.alipay.lookout.common.utils.CommonUtil;
import com.google.common.collect.Sets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import static com.alipay.lookout.core.config.LookoutConfig.DEFAULT_HTTP_EXPORTER_PORT;
import static com.alipay.lookout.core.config.LookoutConfig.LOOKOUT_EXPORTER_ACCESS_TOKEN;

/**
 * @author xiangfeng.xzc
 * @since 2018/7/17
 */
public class MetricsHttpExporter {
    static final Logger            logger          = LookoutLoggerFactory
                                                       .getLogger(MetricsHttpExporter.class);
    private static final Charset   UTF8            = Charset.forName("UTF-8");
    private static final int       DEFAULT_BACKLOG = 2;
    private final PollerController controller;
    private final int              port;
    private final int              backlog;
    private HttpServer             httpServer;

    public MetricsHttpExporter(PollerController controller) {
        this(controller, DEFAULT_HTTP_EXPORTER_PORT, DEFAULT_BACKLOG);
    }

    public MetricsHttpExporter(PollerController controller, int port, int backlog) {
        this.controller = controller;
        this.port = port;
        this.backlog = backlog;
    }

    /**
     * 启动exporter, 暴露底层的http端口
     *
     * @throws IOException IOException
     */
    public void start() throws IOException {
        final ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10),
            CommonUtil.getNamedThreadFactory("client-exporter-pool"),
            new ThreadPoolExecutor.CallerRunsPolicy());

        httpServer = HttpServer.create(new InetSocketAddress(port), backlog);
        httpServer.setExecutor(singleThreadPool);
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
        getController().close();
    }

    /**
     * 用于get数据
     */
    private final HttpHandler getHandler   = new HttpHandler() {
                                               @Override
                                               public void handle(HttpExchange exchange)
                                                                                        throws IOException {
                                                   try {
                                                       if (!isAccessAllowed(exchange)) {
                                                           sendErrResponse(exchange, 403,
                                                               "Forbidden");
                                                           return;
                                                       }

                                                       // 解析参数
                                                       Set<Long> success = Collections.emptySet();
                                                       long newStep = controller.getStep();
                                                       int newSlotCount = controller.getSlotCount();

                                                       try {
                                                           for (NameValuePair nvp : parseParams(exchange)) {
                                                               String name = nvp.getName();
                                                               String value = nvp.getValue();
                                                               if ("step".equalsIgnoreCase(name)) {
                                                                   newStep = Long.parseLong(value);
                                                               } else if ("slotCount"
                                                                   .equalsIgnoreCase(name)) {
                                                                   newSlotCount = Integer
                                                                       .parseInt(value);
                                                               } else if ("success"
                                                                   .equalsIgnoreCase(name)) {
                                                                   success = parseCursors(value);
                                                               }
                                                           }
                                                       } catch (NumberFormatException nfe) {
                                                           sendErrResponse(exchange, 400,
                                                               nfe.getMessage());
                                                           return;
                                                       }

                                                       Object data = controller
                                                           .getNextData(success);

                                                       JSONObject bodyEntity = new JSONObject();
                                                       // 这里返回newStep给用户 表明我们已经接受了用户修改的step
                                                       bodyEntity.put("step", newStep);
                                                       bodyEntity.put("slotCount", newSlotCount);
                                                       bodyEntity.put("data", data);
                                                       sendResponse(exchange, bodyEntity);

                                                       controller.update(newStep, newSlotCount);

                                                       // if (oldRate != newStep || oldSlotCount != newSlotCount) {
                                                       // }
                                                   } catch (Throwable e) {
                                                       logger.warn("pull metrics failed."
                                                                   + e.getMessage());
                                                   } finally {
                                                       exchange.close();
                                                   }
                                               }
                                           };

    private final HttpHandler clearHandler = new HttpHandler() {
                                               @Override
                                               public void handle(HttpExchange exchange)
                                                                                        throws IOException {
                                                   try {
                                                       if (!isAccessAllowed(exchange)) {
                                                           sendErrResponse(exchange, 403,
                                                               "Forbidden");
                                                           return;
                                                       }
                                                       controller.clear();
                                                       exchange.sendResponseHeaders(204, -1);
                                                   } finally {
                                                       exchange.close();
                                                   }
                                               }
                                           };

    private boolean isAccessAllowed(HttpExchange exchange) {
        if (controller.getMetricConfig().containsKey(LOOKOUT_EXPORTER_ACCESS_TOKEN)) {
            //check access token
            String requestToken = exchange.getRequestHeaders().getFirst("X-Lookout-Token");
            if (!StringUtils.equals(requestToken,
                controller.getMetricConfig().getString(LOOKOUT_EXPORTER_ACCESS_TOKEN))) {

                return false;
            }
        }
        return true;
    }

    private static void sendErrResponse(HttpExchange exchange, int httpErrorStatus, String errorMsg)
                                                                                                    throws IOException {
        byte[] data = errorMsg.getBytes();
        exchange.sendResponseHeaders(httpErrorStatus, data.length);
        exchange.getResponseBody().write(data);
        exchange.getResponseBody().close();
    }

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
     * @return the params
     */
    private static List<NameValuePair> parseParams(HttpExchange exchange) {
        return new URIBuilder(exchange.getRequestURI()).getQueryParams();
    }

    public PollerController getController() {
        return controller;
    }
}
