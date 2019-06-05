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
package com.alipay.sofa.lookout.server.prom.common;

import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin.luy@alipay.com on 2018/7/16.
 */
public final class DebugLog {
    private static final ThreadLocal<DebugLog> DEBUG_LOG_TL = new ThreadLocal<DebugLog>();
    private static final Logger                logger       = LoggerFactory.getLogger("debug.log");

    private String                             id;
    private String                             tsdbRequest;
    private String                             tsdbResponse;
    private long                               duration;

    private long                               start;
    private long                               end;
    private long                               step;
    private String                             query;
    private String                             realQuery;

    private List<NativeQueryInfo>              queryInfoList;

    private DebugLog() {
    }

    @Override
    public String toString() {
        return "| " + id + " | " + tsdbRequest + " | " + tsdbResponse + " | " + duration + " |";
    }

    public static boolean isDebugEnable() {
        return DEBUG_LOG_TL.get() != null;
    }

    public static void recTsdbRequest(String tsdbRequest) {
        DebugLog debugLog = DEBUG_LOG_TL.get();
        if (debugLog == null) {
            return;
        }
        debugLog.tsdbRequest = tsdbRequest;
    }

    public static void recTsdbResponse(String tsdbResponse) {
        DebugLog debugLog = DEBUG_LOG_TL.get();
        if (debugLog == null) {
            return;
        }
        debugLog.tsdbResponse = hashText(tsdbResponse);
    }

    private static String hashText(String input) {
        return Hashing.md5().hashBytes(input.getBytes()).toString();
    }

    /**
     * 记录执行耗时
     *
     * @param duration 毫秒
     */
    public static void recDuration(long duration) {
        DebugLog debugLog = DEBUG_LOG_TL.get();
        if (debugLog == null) {
            return;
        }
        debugLog.duration = duration;
    }

    /**
     * @param debugId debug的标记符(方便用户寻找)，可以为没有
     * @return
     */
    public static DebugLog start(String debugId) {
        DebugLog debugLog = DEBUG_LOG_TL.get();
        if (debugLog == null) {
            debugLog = new DebugLog();
            DEBUG_LOG_TL.set(debugLog);
        }
        debugLog.id = debugId;
        return debugLog;
    }

    public static void stop() {
        DebugLog debugLog = DEBUG_LOG_TL.get();
        if (debugLog == null) {
            return;
        }
        logger.info(debugLog.toString());
        DEBUG_LOG_TL.remove();
    }

    public static void addNativeQueryInfoList(NativeQueryInfo info) {
        DebugLog log = DEBUG_LOG_TL.get();
        if (log != null) {
            if (log.queryInfoList == null) {
                log.queryInfoList = new ArrayList<>();
            }
            log.queryInfoList.add(info);
        }
    }

    public List<NativeQueryInfo> getNativeQueryInfoList() {
        return queryInfoList;
    }

    public long getDuration() {
        return duration;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getQuery() {
        return query;
    }

    public String getRealQuery() {
        return realQuery;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setRealQuery(String realQuery) {
        this.realQuery = realQuery;
    }
}
