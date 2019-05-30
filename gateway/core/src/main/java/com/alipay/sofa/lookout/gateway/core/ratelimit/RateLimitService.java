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
package com.alipay.sofa.lookout.gateway.core.ratelimit;

/**
 * 限制客户端上传的速度, 单位是 次/秒, -1表示不限制
 *
 * @author xiangfeng.xzc
 * @date 2018/10/23
 */
public interface RateLimitService {
    /**
     * key需要amount的量
     *
     * @param key
     * @param amount
     * @return
     */
    boolean tryAcquire(String key, int amount);

    /**
     * 获得指定的key的限速值, -1表示不限制
     *
     * @param key
     * @return
     */
    int getKeyLimit(String key);
}
