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
package com.alipay.lookout.dropwizard.metrics;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Tag;

/**
 * http://metrics20.org/
 * <p>
 * Created by kevin.luy@alipay.com on 2017/1/26.
 */
final class NameUtils {

    private NameUtils() {
    }

    /**
     * Convert a dimensional metric id {@slink Id} to  a hierarchical metric name.
     *
     * @param id a dimensional metric id
     * @return hierarchical metric name
     */
    static String toMetricName(Id id) {
        StringBuilder buf = new StringBuilder();
        buf.append(id.name());
        for (Tag t : id.tags()) {
            buf.append('.').append(t.key()).append('-').append(t.value());
        }
        return buf.toString();
    }

}