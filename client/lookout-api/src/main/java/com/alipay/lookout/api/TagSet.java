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

import java.util.*;

/**
 * An immutable set of tags.
 * 1.sorted by the tag key;
 * 2.unique tag key;
 * 3.compatible with JDK6;
 *
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
final class TagSet implements Iterable<Tag> {

    /**
     * tagkey-tag;
     * value(tag) can be reset;
     */
    private final TreeMap<String, Tag> tags;

    static final TagSet                EMPTY = new TagSet(new TreeMap<String, Tag>());

    /**
     * Create a new tag set.
     */
    static TagSet create(Iterable<Tag> tags) {
        return EMPTY.addAll(tags);
    }

    /**
     * Create a new tag set.
     */
    static TagSet create(Map<String, String> tags) {
        return EMPTY.addAll(tags);
    }

    public TagSet(TreeMap<String, Tag> tags) {
        this.tags = tags;
    }

    @Override
    public Iterator<Tag> iterator() {
        return Collections.unmodifiableCollection(tags.values()).iterator();
    }

    boolean isEmpty() {
        return tags.isEmpty();
    }

    /**
     * Add a new tag to the set.
     */
    TagSet add(String k, String v) {
        return add(new BasicTag(k, v));
    }

    /**
     * Add a new tag to the set.
     */
    TagSet add(Tag tag) {
        if (tag == null)
            return this;
        TreeMap<String, Tag> newTags = new TreeMap<String, Tag>(tags);
        //add(or update) a new tag
        newTags.put(tag.key(), tag);
        //generate new one
        return new TagSet(newTags);
    }

    /**
     * Add a collection of tags to the set.
     */
    TagSet addAll(Iterable<Tag> ts) {
        if (ts instanceof TagSet) {
            TagSet data = (TagSet) ts;
            return addAll(data.tags.values());
        }
        Iterator<Tag> it = ts.iterator();
        TreeMap<String, Tag> newTags = new TreeMap<String, Tag>(tags);
        while (it.hasNext()) {
            BasicTag tag = BasicTag.of(it.next());
            newTags.put(tag.key(), tag);
        }
        return new TagSet(newTags);
    }

    /**
     * Add a collection of tags to the set.
     */
    TagSet addAll(Map<String, String> ts) {
        if (ts == null || ts.isEmpty()) {
            return this;
        }
        TreeMap<String, Tag> newTags = new TreeMap<String, Tag>(tags);
        for (Map.Entry<String, String> entry : ts.entrySet()) {
            newTags.put(entry.getKey(), new BasicTag(entry.getKey(), entry.getValue()));
        }
        return new TagSet(newTags);
    }

    /**
     * Add a collection of tags to the set.
     */
    TagSet addAll(String[] ts) {
        Assert.checkArg(ts.length % 2 == 0,
            "the length of the array type argument must be even！length：" + ts.length);
        if (ts.length == 0) {
            return this;
        }
        TreeMap<String, Tag> newTags = new TreeMap<String, Tag>(tags);
        int length = ts.length / 2;
        for (int i = 0; i < length; i++) {
            final int j = i * 2;
            newTags.put(ts[j], new BasicTag(ts[j], ts[j + 1]));
        }
        return new TagSet(newTags);
    }

    /**
     * Add a collection of tags to the set.
     */
    TagSet addAll(Tag[] ts) {
        if (ts.length == 0) {
            return this;
        }
        TreeMap<String, Tag> newTags = new TreeMap<String, Tag>(tags);
        for (int i = 0; i < ts.length; ++i) {
            BasicTag basicTag = BasicTag.of(ts[i]);
            newTags.put(basicTag.key(), basicTag);
        }
        return new TagSet(newTags);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TagSet other = (TagSet) o;
        if (tags.size() != other.tags.size())
            return false;

        return tags.equals(other.tags);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(tags.values().toArray());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(tags.values());
        return builder.toString();
    }
}
