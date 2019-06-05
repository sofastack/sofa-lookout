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
package com.alipay.sofa.lookout.server.interfaces.exception;

import org.springframework.util.StringUtils;

/**
 * Created by kevin.luy@alipay.com on 2018/5/6.
 */
public abstract class PromQueryException extends RuntimeException {
    private String rawQuery  = "";
    private String realQuery = "";

    public PromQueryException() {
    }

    public PromQueryException(String message) {
        super(message);
    }

    public PromQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromQueryException(Throwable cause) {
        super(cause);
    }

    public PromQueryException(String message, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PromQueryException setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
        return this;
    }

    public PromQueryException setRealQuery(String realQuery) {
        this.realQuery = realQuery;
        return this;
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (!StringUtils.isEmpty(rawQuery)) {
            msg += " rawQuery:" + rawQuery;
        }
        if (!StringUtils.isEmpty(realQuery)) {
            msg += " realQuery:" + realQuery;
        }
        return msg;
    }
}
