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
package com.alipay.sofa.lookout.server.storage.ext.es;

import com.alipay.sofa.lookout.server.prom.storage.query.MetadataStatement;

import java.util.List;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-04-29 15:07
 **/
public class ElasticSearchLabelNamesStmt implements MetadataStatement {
    private String queryContent;
    private long   size;

    @Override
    public String getQueryContent() {
        return queryContent;
    }

    @Override
    public long getSize() {
        return size;
    }

    public MetadataStatement setQueryContent(String queryContent) {
        this.queryContent = queryContent;
        return this;
    }

    @Override
    public MetadataStatement setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public List<String> executeQuery() {
        return null;
    }
}
