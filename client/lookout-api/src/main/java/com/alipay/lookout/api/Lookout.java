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
package com.alipay.lookout.api;

import com.alipay.lookout.common.Assert;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 正常情况下，不建议使用该方式获取全局 registry.
 * 需要显示设置后才能使用，或者与 @see com.alipay.lookout.client.SimpleLookoutClient 搭配使用
 * Created by kevin.luy@alipay.com on 2017/2/19.
 */
public final class Lookout {
    private static final AtomicReference<Registry> atomicRegistryReference = new AtomicReference<Registry>(
                                                                               NoopRegistry.INSTANCE);

    public static Registry registry() {
        return atomicRegistryReference.get();
    }

    /**
     * this method can only invoke once,but you can set a compositeRegistry.
     *
     * @param registry registry
     */
    public static void setRegistry(Registry registry) {
        //只能设置一次；
        if (registry == NoopRegistry.INSTANCE) {
            return;
        }
        Assert.state(atomicRegistryReference.compareAndSet(NoopRegistry.INSTANCE, registry), String
            .format("Global registry can only reset one time! current is %s",
                atomicRegistryReference.get()));

        //TODO 切换时，warning哪些老的注册
    }

    private Lookout() {
    }
}
