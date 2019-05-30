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
package com.alipay.sofa.lookout.gateway.core.common;

/**
 * 持有一个boolean值, 用于表示是否要拒绝请求 之前是将这个boolean值放在 Configuration 里, 感觉这样不好, 可以单独抽出一个类来做
 *
 * @author xiangfeng.xzc
 * @date 2018/11/22
 */
public interface RefuseRequestService {
    boolean isRefuseRequest();

    void setRefuseRequest(boolean refuseRequest);
}
