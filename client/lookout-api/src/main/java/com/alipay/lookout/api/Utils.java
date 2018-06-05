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

/**
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public final class Utils {
    private Utils() {
    }

    /**
     * get the value of a tag which belongs to an id.
     *
     * @param id metricId
     * @param tagKey tag key
     * @return tagValue
     */
    public static String getTagValue(Id id, String tagKey) {
        Assert.notNull(id, "id");
        return getTagValue(id.tags(), tagKey);
    }

    /**
     * @param tags   tag collection
     * @param tagKey tag key
     * @return if the tag key can't be found,a null value will be returned.
     */
    public static String getTagValue(Iterable<Tag> tags, String tagKey) {
        Assert.notNull(tags, "tags");
        Assert.notNull(tagKey, "tag-key");
        for (Tag t : tags) {
            if (tagKey.equals(t.key())) {
                return t.value();
            }
        }
        return null;
    }

    /**
     * Convert a dimensional metric id {@slink Id} to  a hierarchical metric name.
     *
     * @param id a dimensional metric id
     * @return hierarchical metric name
     */
    public static String toMetricName(Id id) {
        StringBuilder buf = new StringBuilder();
        buf.append(id.name());
        for (Tag t : id.tags()) {
            buf.append('.').append(t.key()).append('-').append(t.value());
        }
        return buf.toString();
    }
}
