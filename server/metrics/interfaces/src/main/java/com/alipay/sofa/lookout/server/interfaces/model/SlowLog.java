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
package com.alipay.sofa.lookout.server.interfaces.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * @author xiangfeng.xzc
 * @date 2018/8/7
 */
public class SlowLog {
    /**
     * 查询范围-开始时间
     */
    private String       start;
    /**
     * 查询范围-结束时间
     */
    private String       end;
    /**
     * 查询范围-步长
     */
    private String       step;
    private long         duration;
    private String       query;
    private List<String> exclusions;

    public SlowLog(String start, String end, String step, long duration, List<String> exclusions,
                   String query) {
        this.start = start;
        this.end = end;
        this.step = step;
        this.duration = duration;
        this.exclusions = exclusions;
        this.query = query;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime start = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(Long.parseLong(this.start)), zoneId);
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(this.end)),
            zoneId);
        sb.append(duration).append("ms|");
        sb.append(query).append('|');
        sb.append(start).append('|');
        sb.append(end).append('|');
        sb.append(step).append("s|");
        sb.append(exclusions != null ? exclusions.toString() : "");
        return sb.toString();
    }
}
