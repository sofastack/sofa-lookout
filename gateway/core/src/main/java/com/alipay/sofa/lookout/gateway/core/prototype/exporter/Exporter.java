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
package com.alipay.sofa.lookout.gateway.core.prototype.exporter;


import com.alipay.sofa.lookout.gateway.core.common.DataType;
import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycle;

/**
 * @author: kevin.luy@antfin.com
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public interface Exporter<T> extends LifeCycle {
    /**
     * TODO name 和 type要利用起来 不是一个简单的字符串而已...
     *
     * @return
     */
    String getName();

    /**
     * 是否支持指定的类型
     *
     * @param dataType
     * @return
     */
    boolean supports(DataType dataType);

    /**
     * TODO 是否还需要上下文信息
     *
     * @param t t要导出的数据
     */
    default void export(T t) {
        this.export(t, null);
    }

    /**
     * 带上下文的导出方法, 实现者需要自己强转ctx
     *
     * @param t
     * @param ctx
     */
    default void export(T t, Object ctx) {
    }
}
