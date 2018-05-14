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

import java.util.Arrays;

/**
 * Immutable implementation of Tag.
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
public final class BasicTag implements Tag {

    private final String key;
    private final String value;

    /**
     * create a new basic tag instance.
     *
     * @param key   k
     * @param value v
     */
    public BasicTag(String key, String value) {
        this.key = Assert.notNull(key, "tag key");
        this.value = value == null ? "" : value;
    }

    /**
     * 支持自定义类型tag生成标准的BasicTag
     * @param tag tag
     */
    static BasicTag of(Tag tag) {
        return (tag instanceof BasicTag) ? (BasicTag) tag : new BasicTag(tag.key(), tag.value());
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof BasicTag))
            return false;
        BasicTag tag = (BasicTag) obj;
        return key.equals(tag.key) && value.equals(tag.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new String[] { key, value });
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
