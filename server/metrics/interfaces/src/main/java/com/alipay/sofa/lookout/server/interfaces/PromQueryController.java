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
package com.alipay.sofa.lookout.server.interfaces;

import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.Registry;
import com.alipay.sofa.lookout.server.interfaces.common.NetworkUtil;
import com.alipay.sofa.lookout.server.interfaces.common.Preconditions;
import com.alipay.sofa.lookout.server.interfaces.common.QueryExpressionUtil;
import com.alipay.sofa.lookout.server.interfaces.common.TimestampUtil;
import com.alipay.sofa.lookout.server.interfaces.exception.PromClientException;
import com.alipay.sofa.lookout.server.interfaces.exception.PromServerException;
import com.alipay.sofa.lookout.server.interfaces.model.MatrixResult;
import com.alipay.sofa.lookout.server.interfaces.model.SlowLog;
import com.alipay.sofa.lookout.server.interfaces.model.ValueData;
import com.alipay.sofa.lookout.server.interfaces.model.VectorResult;
import com.alipay.sofa.lookout.server.prom.common.DebugLog;
import com.alipay.sofa.lookout.server.prom.exception.QLParseException;
import com.alipay.sofa.lookout.server.prom.exception.TooManyPointsException;
import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.ql.engine.PromQLEngine;
import com.alipay.sofa.lookout.server.prom.ql.engine.Query;
import com.alipay.sofa.lookout.server.prom.ql.engine.Result;
import com.alipay.sofa.lookout.server.prom.ql.value.Matrix;
import com.alipay.sofa.lookout.server.prom.ql.value.ValueType;
import com.alipay.sofa.lookout.server.prom.ql.value.Vector;
import com.alipay.sofa.lookout.server.prom.storage.query.LabelValuesStatement;
import com.alipay.sofa.lookout.server.prom.storage.query.MetadataStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin.luy@alipay.com on 2018/2/26.
 */
@RequestMapping("/api/v1")
@RestController("promQueryController")
public class PromQueryController {

    private static final Logger SLOG_LOG_LOGGER            = LoggerFactory.getLogger("slow.log");

    private PromQLEngine        engine;

    @Value("${lookout.server.slowLogDuration:500}")
    private int                 slowLogDuration;

    /**
     * 默认情况下, 查询底层数据的时候是否使用原生的底层查询
     */
    @Value("${lookout.server.useNative:false}")
    private boolean             defaultUseNative;

    @Value("${lookout.server.timeCorrection:true}")
    private boolean             defaultTimeCorrection;

    /**
     * 从多久的数据跨度里获取元数据(默认: 1小时)
     */
    @Value("${lookout.server.extractMetaFromDataMinutes:1440}")
    private long                extractMetaFromDataMinutes = 60 * 24;

    public PromQueryController(PromQLEngine engine) {
        this.engine = engine;
    }

    @GetMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public ValueData execInstanceQuery(@RequestParam String query,
                                       @RequestParam(required = false) String time,
                                       @RequestParam(required = false) String timeout) {
        Preconditions.checkNotNull(query);
        QueryExpressionUtil.checkNoTagFilter(query);
        QueryExpressionUtil.checkRegexMatcher(query);
        Instant queryTime = Instant.now();
        if (time != null) {
            try {
                queryTime = Instant.ofEpochMilli(TimestampUtil.sec2mills(time));
            } catch (NumberFormatException nfe) {
                throw new PromClientException(nfe);
            }
        }

        registry().counter(registry().createId("lookout.server.instant.query.count")).inc();
        long startTime = System.currentTimeMillis();
        Result result = null;
        try {
            Query qry = engine.newInstantQuery(query, queryTime);
            result = qry.exec();
        } catch (QLParseException qe) {
            registry().counter(
                registry().createId("lookout.server.instant.query.fail.count").withTag("err",
                    "qlParseException")).inc();
            throw new PromClientException(qe).setRawQuery(query);
        } catch (TooManyPointsException e) {
            registry().counter(
                registry().createId("lookout.server.instant.query.fail.count").withTag("err",
                    "tooManyPointsException")).inc();
            throw new PromClientException(e).setRawQuery(query);
        } catch (Throwable e) {
            registry().counter(registry().createId("lookout.server.instant.query.fail.count"))
                .inc();
            throw new PromServerException(e).setRawQuery(query);
        } finally {
            registry().timer(registry().createId("lookout.server.instant.query.time")).record(
                System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
        }
        if (result.getValue() instanceof Vector) {
            Vector vector = (Vector) result.getValue();
            return ValueData.newValueData(true, ValueType.vector, VectorResult.from(vector));
        } else if (result.getValue() instanceof Matrix) {
            Matrix matrix = (Matrix) result.getValue();
            return ValueData.newValueData(true, ValueType.matrix, MatrixResult.from(matrix));
        } else {
            return ValueData.newValueData(false, ValueType.none, null);
        }

    }

    /**
     * range query
     *
     * @param query
     * @param start          推荐秒，但可以毫秒
     * @param end            推荐秒，但可以毫秒
     * @param step           必须秒.（结果展示的解析度）
     * @param exclusions     LabelNames以逗号链接，查询表达式中需要忽略的label查询条件，比如"app,host"
     * @param timeout
     * @param debug
     * @param useNative      是否尽量使用原生数据库查询, 安全起见默认是false
     * @param timeCorrection 时间纠正是否开启
     * @return
     */
    @GetMapping(value = "/query_range", produces = MediaType.APPLICATION_JSON_VALUE)
    public ValueData execRangeQuery(@RequestParam String query,
                                    @RequestParam String start,
                                    @RequestParam String end,
                                    @RequestParam String step,
                                    @RequestParam(required = false) List<String> exclusions,
                                    @RequestParam(required = false) String timeout,
                                    @RequestParam(required = false) String debug,
                                    @RequestParam(name = "useNative", required = false) Boolean useNative,
                                    @RequestParam(name = "timeCorrection", required = false) Boolean timeCorrection) {

        useNative = useNative == null ? defaultUseNative : useNative;
        timeCorrection = timeCorrection == null ? defaultTimeCorrection : timeCorrection;

        Preconditions.checkNotNull(query);
        String newQuery = QueryExpressionUtil.ignoreLabelMatches(query, exclusions);
        QueryExpressionUtil.checkNoTagFilter(newQuery);
        QueryExpressionUtil.checkRegexMatcher(newQuery);

        //        tokenStatisticService.recordQuery(request, newQuery);

        Result<Matrix> result = null;
        registry().counter(registry().createId("lookout.server.range.query.count")).inc();
        DebugLog debugLog = debug != null ? DebugLog.start(debug) : null;
        long startTime = System.currentTimeMillis();
        try {
            if (debugLog != null) {
                debugLog.setStart(Long.parseLong(start));
                debugLog.setEnd(Long.parseLong(end));
                debugLog.setStep(Integer.parseInt(step));
                debugLog.setQuery(query);
                debugLog.setRealQuery(newQuery);
            }
            long[] startEndTime = getStartEndTime(TimestampUtil.sec2mills(start),
                TimestampUtil.sec2mills(end), TimestampUtil.sec2mills(step), timeCorrection);
            Query qry = engine.newRangeQuery(newQuery, Instant.ofEpochMilli(startEndTime[0]),
                Instant.ofEpochMilli(startEndTime[1]), Duration.ofSeconds(Long.parseLong(step)));
            qry.hints().setUseNative(useNative);
            result = qry.exec();
        } catch (NumberFormatException nfe) {
            registry().counter(registry().createId("lookout.server.range.query.fail.count")).inc();
            throw new PromClientException(nfe).setRawQuery(query).setRealQuery(newQuery);
        } catch (QLParseException qe) {
            registry().counter(
                registry().createId("lookout.server.range.query.fail.count").withTag("err",
                    "qlParseException")).inc();
            throw new PromClientException(qe).setRawQuery(query).setRealQuery(newQuery);
        } catch (TooManyPointsException e) {
            registry().counter(
                registry().createId("lookout.server.range.query.fail.count").withTag("err",
                    "tooManyPointsException")).inc();
            throw new PromClientException(e).setRawQuery(query).setRealQuery(newQuery);
        } catch (Throwable e) {
            registry().counter(registry().createId("lookout.server.range.query.fail.count")).inc();
            throw new PromServerException(e).setRawQuery(query).setRealQuery(newQuery);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            registry().timer(registry().createId("lookout.server.range.query.time")).record(
                duration, TimeUnit.MILLISECONDS);
            DebugLog.recDuration(duration);
            DebugLog.stop();
            if (slowLogDuration > 0 && duration > slowLogDuration
                && SLOG_LOG_LOGGER.isInfoEnabled()) {
                SLOG_LOG_LOGGER.info("{}", new SlowLog(start, end, step, duration, exclusions,
                    query));

            }
        }

        Matrix matrix = result.getValue();
        ValueData valueData = ValueData.newValueData(true, ValueType.matrix,
            MatrixResult.from(matrix));
        valueData.setDebugInfo(buildDebugInfo(debugLog));
        return valueData;
    }

    private static Map<String, Object> buildDebugInfo(DebugLog debugLog) {
        if (debugLog == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("query", debugLog.getQuery());
        map.put("realQuery", debugLog.getRealQuery());
        map.put("start", debugLog.getStart());
        map.put("end", debugLog.getEnd());
        map.put("startStr", Instant.ofEpochMilli(debugLog.getStart()).atZone(ZoneId.systemDefault()));
        map.put("endStr", Instant.ofEpochMilli(debugLog.getEnd()).atZone(ZoneId.systemDefault()));
        map.put("timeRange", Duration.ofMillis(debugLog.getEnd() - debugLog.getStart()));
        map.put("step", debugLog.getStep());
        map.put("mdbQueryInfoList", debugLog.getNativeQueryInfoList());
        map.put("totalDuration", debugLog.getDuration());
        map.put("serverIp", NetworkUtil.getLocalAddress());
        return map;
    }

    /**
     * query all possible values for the labelName 支持的场景包括:(1)根据metricName查询普通tag；（2）根据普通tag查询metricNames； （3） 根据普通tag查询普通tag
     *
     * @param labelName
     * @param size      result size
     * @param lk        label key
     * @param lv        label value
     * @return
     */
    @GetMapping(value = "/label/{labelName}/values", produces = MediaType.APPLICATION_JSON_VALUE)
    public ValueData execLabelQuery(@PathVariable String labelName,
                                    @RequestParam(required = false) Integer size,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(required = false) String[] lk,
                                    @RequestParam(required = false) String[] lv,
                                    @RequestParam(required = false) String instanceId) {

        Preconditions.checkTrue(
            (lk == null && lv == null)
                    || (lk != null && lv != null && lk.length == lv.length && lk.length <= 2),
            "lk&lv is invalid!");
        registry().counter(registry().createId("lookout.server.label.query.count")).inc();
        long startTime = System.currentTimeMillis();
        try {
            if (!labelName.equals(Labels.MetricName) && lk != null) {
                String metricName = "*";
                String parentTagKey = null;
                String parentTagValue = null;
                for (int i = 0; i < lk.length; i++) {
                    if (!lk[i].equalsIgnoreCase(Labels.MetricName)) {
                        parentTagKey = lk[i];
                        parentTagValue = lv[i];
                    } else {
                        metricName = lv[i];
                    }
                }
                //                if (parentTagKey != null) {
                //                    List<String> values = opsApiQuerier.queryLabelValues(metricName, parentTagKey, parentTagValue,
                //                            labelName);
                //                    if (q != null && q.length() > 0) {
                //                        values = values.stream().filter(s -> s.startsWith(q)).collect(Collectors.toList());
                //                    }
                //                    if (size != null && size > 0 && size < values.size()) {
                //                        values = values.subList(0, size);
                //                    }
                //                    return new ValueData(true, values);
                //                }
            }

            Label label = null;
            if (lk != null && lk.length == 1) {
                label = new Label(lk[0], lv[0]);
            }

            LabelValuesStatement lvStmt = engine.getStorage().querier().createLabelValuesStmt();
            lvStmt.setStartTime(Instant.now().minus(Duration.ofMinutes(extractMetaFromDataMinutes))
                .toEpochMilli());
            lvStmt.setEndTime(Instant.now().toEpochMilli());
            lvStmt.setLabelName(labelName);
            lvStmt.setQueryContent(q);
            lvStmt.setFilterLabel(label);
            lvStmt.setSize(size == null ? -1 : size);
            return new ValueData(true, lvStmt.executeQuery());
        } catch (Throwable e) {
            registry().counter(registry().createId("lookout.server.label.query.fail.count")).inc();
            throw new PromServerException(e);
        } finally {
            registry().timer(registry().createId("lookout.server.label.query.time")).record(
                System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Getting label names
     * <p>
     * https://prometheus.io/docs/prometheus/latest/querying/api/#getting-label-names
     *
     * @param q
     * @return
     */
    @GetMapping(value = "/labels", produces = MediaType.APPLICATION_JSON_VALUE)
    public ValueData getLabelKeys(@RequestParam(required = false) String q) {
        MetadataStatement<List<String>> labelNamesStmt = engine.getStorage().querier()
            .createLabelNamesStmt();
        labelNamesStmt.setQueryContent(q);
        labelNamesStmt.setSize(10000);

        //        Querier querier = engine.getQueryable()
        //                .createQuerier(Instant.now().minus(Duration.ofMinutes(extractMetaFromDataMinutes)).toEpochMilli(),
        //                        Instant.now().toEpochMilli());
        registry().counter(registry().createId("lookout.server.label.keys.query.count")).inc();
        long startTime = System.currentTimeMillis();
        try {
            return new ValueData(true, labelNamesStmt.executeQuery());
        } catch (Throwable e) {
            registry().counter(registry().createId("lookout.server.label.keys.query.fail.count"))
                .inc();
            throw new PromServerException(e);
        } finally {
            registry().timer(registry().createId("lookout.server.label.keys.query.time")).record(
                System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
        }
    }

    private Registry registry() {
        return Lookout.registry();
    }

    /**
     * @param startTime      milliseconds
     * @param endTime        milliseconds
     * @param interval
     * @param timeCorrection true an step 进行endTime的时间纠正
     * @return corrected start time and end time
     */
    long[] getStartEndTime(long startTime, long endTime, long interval, boolean timeCorrection) {
        long[] timeRange = new long[] { startTime, endTime };
        if (timeCorrection) {
            long range = endTime - startTime;
            //15*60*1000[15min]
            if (range < 900000) {
                return timeRange;
            }
            //分钟级别对齐
            endTime = endTime / 60000 * 60000;
            startTime = endTime - range;
            timeRange[0] = startTime;
            timeRange[1] = endTime;
        }
        return timeRange;
    }
}
