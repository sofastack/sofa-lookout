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
package com.alipay.lookout.common.utils;

import com.alipay.lookout.api.PRIORITY;
import com.alipay.lookout.api.Tag;
import com.alipay.lookout.api.Utils;

import static com.alipay.lookout.common.LookoutConstants.TAG_PRIORITY_KEY;

/**
 * Created by kevin.luy@alipay.com on 2017/2/24.
 */
public final class PriorityTagUtil {
    private PriorityTagUtil() {
    }

    public static PRIORITY resolve(Iterable<Tag> tags) {
        //default
        PRIORITY v = PRIORITY.NORMAL;

        String value = Utils.getTagValue(tags, TAG_PRIORITY_KEY);
        if (value != null) {
            for (PRIORITY p : PRIORITY.values()) {
                if (p.name().equalsIgnoreCase(value)) {
                    return p;
                }
            }
        }
        return v;
    }
}
