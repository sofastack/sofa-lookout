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
package com.alipay.sofa.lookout.server.prom;

import com.alipay.sofa.lookout.server.prom.labels.Label;
import com.alipay.sofa.lookout.server.prom.labels.Labels;
import com.alipay.sofa.lookout.server.prom.ql.value.Series;
import com.alipay.sofa.lookout.server.prom.ql.value.Series.Point;
import com.alipay.sofa.lookout.server.test.ServerTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yuanxuan
 * @version $Id: TestBase.java, v 0.1 2019年05月28日 12:49 yuanxuan Exp $
 */
public class TestBase extends ServerTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestBase.class);

    protected List<Series> loadMockSeries(String name) {
        List<Series> seriesList = new ArrayList<>();
        try {
            Map<Integer, Series> seriesMap = new HashMap<>();
            loadMockResponse(name, data -> {
                String[] arr = data.split(",");
                String timestamp = arr[0];
                String value = arr[1];
                int labelIndex = timestamp.length() + value.length() + 2;
                String labels = data.substring(labelIndex);
                int labelHC = labels.hashCode();
                Date time = dateWithZoned(timestamp);
                Series s = seriesMap.get(labelHC);
                if (s == null) {
                    s = new Series(new Labels());
                    for (String label : labels.split(",")) {
                        String[] labelsArr = label.split("=");
                        s.getMetric().add(new Label(labelsArr[0], labelsArr[1]));
                    }
                    seriesMap.put(labelHC, s);
                }
                Point point = new Point(time.getTime(), Double.valueOf(value));
                s.add(point);
            });
            seriesList.addAll(seriesMap.values());
            return seriesList;
        } catch (IOException e) {
            LOGGER.error("load mock data error.", e);
            return seriesList;
        }
    }

    protected Date dateWithZoned(String dateStr) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
            return Date.from(zonedDateTime.toInstant());
        } catch (Exception e) {
            LOGGER.error("parse date error from :{}", dateStr, e);
            return null;
        }
    }

    protected Instant instantWithZoned(String dateStr) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
            return zonedDateTime.toInstant();
        } catch (Exception e) {
            LOGGER.error("parse date error from :{}", dateStr, e);
            return null;
        }
    }

}