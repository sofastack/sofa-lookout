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
package com.alipay.sofa.lookout.gateway.core.prototype.exporter.chain.ipml;

import com.alipay.sofa.lookout.gateway.core.prototype.exporter.Exporter;
import com.alipay.sofa.lookout.gateway.core.prototype.exporter.chain.AbstractExportChain;
import com.alipay.sofa.lookout.gateway.core.prototype.filter.Filter;
import com.alipay.sofa.lookout.gateway.core.utils.ListUtils;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * 静态的chain, filters和exporter都不会变
 *
 * @author xiangfeng.xzc
 * @date 2018/11/14
 */
public class StaticExportChain<T> extends AbstractExportChain<T> {
    protected final Exporter<T>     exporter;
    protected final List<Filter<T>> filters;

    public StaticExportChain(Exporter<T> exporter) {
        this(exporter, null);
    }

    public StaticExportChain(Exporter<T> exporter, List<Filter<T>> filters) {
        this.exporter = Preconditions.checkNotNull(exporter);
        this.filters = ListUtils.unmodifiableList(filters);
    }

    @Override
    protected boolean doFilter(T t) {
        for (Filter<T> filter : filters) {
            Filter.FilterResult result = filter.test(t, null);
            if (result != Filter.SUCCESS) {
                // TODO 记录失败信息
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doExport(T t) {
        exporter.export(t);
    }

    @Override
    public Exporter<T> exporter() {
        return exporter;
    }
}
