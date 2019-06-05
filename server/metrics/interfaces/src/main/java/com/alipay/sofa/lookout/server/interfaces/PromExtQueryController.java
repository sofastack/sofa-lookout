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
//package com.alipay.sofa.lookout.server.interfaces;
//
//import com.alipay.lookout.api.Lookout;
//import com.alipay.lookout.api.Registry;
//import com.alipay.lookout.ops.client.user.UserContext;
//import com.alipay.lookout.ops.client.user.UserPrincipal;
//import com.alipay.sofa.lookout.server.prom.ql.engine.PromQLEngine;
//import com.google.common.base.Preconditions;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by kevin.luy@alipay.com on 2018/2/26.
// */
//@RequestMapping("/api/v1")
//@RestController("promExtQueryController")
//public class PromExtQueryController {
//
//    private static final List<String> FUNCS = Arrays.asList("sum", "count", "max", "min",
//            "avg", "none");
//
//    private PromQLEngine engine;
//
//    /**
//     * 从多久的数据跨度里获取元数据(默认: 1小时)
//     */
//    @Value("${lookout.server.extractMetaFromDataMinutes:1440}")
//    private long extractMetaFromDataMinutes = 60 * 24;
//
//    @Autowired
//    private OpsApiQuerier opsApiQuerier;
//
//    @Autowired
//    private PromQueryController promQueryController;
//
//    public PromExtQueryController(PromQLEngine engine) {
//        this.engine = engine;
//    }
//
//
//    /**
//     * 数据实验台,将 Rest 请求格式加工为 PromQL 的形式;
//     *
//     * @param request
//     * @param metric
//     * @param function
//     * @param labels
//     * @param groups
//     * @param start
//     * @param end
//     * @param step
//     * @param timeout
//     * @param debug
//     * @param useNative
//     * @return
//     */
//    @GetMapping(value = "/ext/query", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ValueData execRangeQuery(HttpServletRequest request,
//                                    @RequestParam String metric,
//                                    @RequestParam String function,
//                                    @RequestParam String labels,
//                                    @RequestParam String groups,
//                                    @RequestParam String start,
//                                    @RequestParam String end,
//                                    @RequestParam String step,
//                                    @RequestParam(required = false) String timeout,
//                                    @RequestParam(required = false) String debug,
//                                    @RequestParam(name = "useNative", required = false) Boolean useNative) {
//        String query = QueryBuilder.buildQuery(metric, function, labels, groups, step);
//        ValueData data = promQueryController.execRangeQuery(request, query, start, end, step, Collections.EMPTY_LIST,
//                timeout, debug, useNative, false);
//        if (data.getStatus().equals("success")) {
//            ValueData.Data resultData = (ValueData.Data) data.getData();
//            data.setData(new QueryResult(query, resultData.getResult()));
//        }
//        return data;
//    }
//
//    /**
//     * (1) 根据前缀查询 metricName候选集
//     * GET /api/v1/label/__name__/values?q={metric_name_prefix}&size={size}
//     *
//     * @param labelName
//     * @param metricName
//     * @param labels
//     * @param q
//     * @param size
//     * @param instanceId
//     * @return
//     */
//    @GetMapping(value = "/ext/label/{labelName}/values", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ValueData execLabelQuery(@PathVariable String labelName,
//                                    @RequestParam(required = false) String metricName,
//                                    @RequestParam(required = false) String labels,
//                                    @RequestParam(required = false) String q,
//                                    @RequestParam(defaultValue = "100") Integer size,
//                                    @RequestParam(required = false) String instanceId) {
//
//        registry().counter(registry().createId("lookout.server.label.query.count")).inc();
//        long startTime = System.currentTimeMillis();
//        try {
//            if (StringUtils.isBlank(labels)) {
//                Querier querier = engine.getQueryable().createQuerier(
//                        Instant.now().minus(Duration.ofMinutes(extractMetaFromDataMinutes)).toEpochMilli(),
//                        Instant.now().toEpochMilli());
//                Label label = new Label(Labels.MetricName, metricName);
//                return new ValueData(true, querier.labelValuesFor(labelName, q, label,
//                        size == null ? -1 : size, instanceId));
//            } else {
//                List<String> values = opsApiQuerier.queryLabelValuesExt(metricName, labels,
//                        labelName, q, size);
//                return new ValueData(true, values);
//            }
//        } catch (Throwable e) {
//            registry().counter(registry().createId("lookout.server.label.query.fail.count")).inc();
//            throw new PromServerException(e);
//        } finally {
//            registry().timer(registry().createId("lookout.server.label.query.time")).record(
//                    System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
//        }
//    }
//
//
//    /**
//     * (2) 查询metric有哪些tag
//     * GET /api/v1/ext/labels?q={metric_name}
//     *
//     * @param q
//     * @return
//     */
//    @GetMapping(value = "/ext/labels", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ValueData getLabelKeys2(@RequestParam(required = false) String q) {
//        Querier querier = engine.getQueryable()
//                .createQuerier(Instant.now().minus(Duration.ofMinutes(extractMetaFromDataMinutes)).toEpochMilli(),
//                        Instant.now().toEpochMilli());
//        registry().counter(registry().createId("lookout.server.label.keys.query.count")).inc();
//        long startTime = System.currentTimeMillis();
//        try {
//            return new ValueData(true, querier.labelKeys(q));
//        } catch (Throwable e) {
//            registry().counter(registry().createId("lookout.server.label.keys.query.fail.count"))
//                    .inc();
//            throw new PromServerException(e);
//        } finally {
//            registry().timer(registry().createId("lookout.server.label.keys.query.time")).record(
//                    System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
//        }
//    }
//
//    /**
//     * 查询用户所拥有的app列表
//     *
//     * @param owner
//     * @return
//     */
//    @GetMapping(value = "/ext/ownership/apps", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ValueData getAppsByOwner(@RequestParam(required = false) String owner) {
//        registry().counter(registry().createId("lookout.server.ownership.query.count")).inc();
//        long startTime = System.currentTimeMillis();
//        try {
//            UserPrincipal user = UserContext.getUser();
//            if (user != null && user != UserContext.mockUser()) {
//                String email = user.getEmail();
//                if (email != null && email.length() > 0) {
//                    owner = email;
//                }
//            }
//            Preconditions.checkTrue(owner != null & owner.length() > 0, "owner can not be empty");
//            return new ValueData(true, opsApiQuerier.queryAppsByOwner(owner));
//        } catch (Throwable e) {
//            registry().counter(registry().createId("lookout.server.ownership.query.fail.count"))
//                    .inc();
//            throw new PromServerException(e);
//        } finally {
//            registry().timer(registry().createId("lookout.server.ownership.query.time")).record(
//                    System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
//        }
//    }
//
//    /**
//     * 查询可用函数及其相关信息
//     * GET /api/v1/ext/funcs
//     *
//     * @return
//     */
//    @GetMapping(value = "/ext/funcs", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ValueData getFunctions() {
//        registry().counter(registry().createId("lookout.server.funcs.query.count")).inc();
//        long startTime = System.currentTimeMillis();
//        try {
//            return new ValueData(true, FUNCS);
//        } catch (Throwable e) {
//            registry().counter(registry().createId("lookout.server.funcs.query.fail.count")).inc();
//            throw new PromServerException(e);
//        } finally {
//            registry().timer(registry().createId("lookout.server.funcs.query.time")).record(
//                    System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
//        }
//    }
//
//    private Registry registry() {
//        return Lookout.registry();
//    }
//}
