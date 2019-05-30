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
package com.alipay.sofa.lookout.server.common.es.operation;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-12 16:39
 **/
public class ESOperatorBuilder {
    String     index;
    String     alias;
    String     searchAlias;
    String     mappingType;
    String     mappingContent;
    String     hostUrl;
    ESDataType dataType;
    boolean    autoScanEnable = false;
    String     maxAge         = "1d";
    long       maxDocs        = 100000000;
    long       validDays      = 7;

    public ScheduledExecutorService getScheduler() {
        return SingletonHolder.SCHEDULER;
    }

    public ESOperatorBuilder(ESDataType ESDataType) {
        this.dataType = ESDataType;
    }

    public ESOperatorBuilder httpHost(String httpHostUrl) {
        this.hostUrl = httpHostUrl;
        return this;
    }

    public ESOperatorBuilder index(String index) {
        Preconditions.checkNotNull(index);
        this.index = index;
        alias = "lookout-active-" + index;
        searchAlias = "lookout-search-" + index;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ESOperatorBuilder mapping(String type, String mappingContent) {
        this.mappingType = type;
        this.mappingContent = mappingContent;
        return this;

    }

    public ESOperatorBuilder addRolloverTask(String maxAge, long maxDocs) {
        this.maxAge = maxAge;
        this.maxDocs = maxDocs;
        autoScanEnable = true;
        return this;

    }

    public ESOperatorBuilder addDropOldIndicesTask(int days) {
        this.validDays = days;
        autoScanEnable = true;
        return this;
    }

    public ESOperator build() {
        Preconditions.checkNotNull(hostUrl, "es host url is required!");
        return new ESOperator(this);
    }

    private static class SingletonHolder {

        private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(
                                                                    2,
                                                                    new ThreadFactoryBuilder()
                                                                        .setNameFormat(
                                                                            "ES-OPERATOR-%d")
                                                                        .setDaemon(true).build());
    }

}
