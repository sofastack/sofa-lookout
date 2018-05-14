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
package com.alipay.lookout.api.info;

/**
 * 自动采集友好info
 * Created by kevin.luy@alipay.com on 2017/2/22.
 */
public interface AutoPollFriendlyInfo<T> extends Info<T> {
    /**
     * auto-poll(collect) suggest
     * (非数值类型metric 可能数据过大，或计算value（）耗时，所以自身可以给出自动采集器的建议)
     * @return  autoPollSuggestion
     */
    AutoPollSuggestion autoPollSuggest();

    /**
     * last modified timestamp; (Work with AutoPollSuggestion.POLL_WHEN_UPDATED)
     *
     * @return millisecond
     */
    long lastModifiedTime();
}
