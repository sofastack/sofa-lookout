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
package com.alipay.lookout;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2017/3/30.
 */
public class MockId implements Id {

    private String    name;
    private List<Tag> tags = new ArrayList<Tag>();

    public MockId(String aa) {
        this.name = aa;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Iterable<Tag> tags() {
        return tags;
    }

    @Override
    public Id withTag(final String k, final String v) {
        withTag(new Tag() {
            @Override
            public String key() {
                return k;
            }

            @Override
            public String value() {
                return v;
            }
        });
        return this;
    }

    @Override
    public Id withTag(Tag t) {
        tags.add(t);
        return this;
    }

    @Override
    public Id withTags(Tag... tags) {
        return null;
    }

    @Override
    public Id withTags(Iterable<Tag> tags) {
        return null;
    }

    @Override
    public Id withTags(Map<String, String> tags) {
        return null;
    }
}
