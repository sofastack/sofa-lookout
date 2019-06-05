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
package com.alipay.sofa.lookout.server.prom.labels;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * Label （Tag）is a kv pair of strings.
 * <p>
 * Created by kevin.luy@alipay.com on 2018/2/7.
 */
public class Label {
    private String name;
    private String value;

    public Label(String name, String value) {
        Preconditions.checkNotNull(name, "label name can not be null!");
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return 31 * (name == null ? 0 : name.hashCode()) + (value == null ? 0 : value.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Label))
            return false;
        Label l2 = (Label) obj;
        return StringUtils.equals(name, l2.name) && StringUtils.equals(value, l2.value);
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
