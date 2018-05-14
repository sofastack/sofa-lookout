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
package com.alipay.lookout.common;

import com.alipay.lookout.api.BasicTag;
import com.alipay.lookout.api.Tag;

/**
 * Created by kevin.luy@alipay.com on 2017/2/7.
 */
public interface LookoutConstants {

    String DOT               = ".";

    String TAG_PRIORITY_KEY  = "priority";
    String TAG_GROUP_KEY     = "group";

    Tag    HIGH_PRIORITY_TAG = new BasicTag(TAG_PRIORITY_KEY, "high");

    Tag    LOW_PRIORITY_TAG  = new BasicTag(TAG_PRIORITY_KEY, "low");

    //ant cloud shared middleware identification
    String INSTANCE_ID_NAME  = "instance_id";

}
