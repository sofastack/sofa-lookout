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

/**
 * valid args and check state
 */
public final class Assert {
    private Assert() {
    }

    public static <T> T notNull(T obj, String name) {
        if (obj == null) {
            String msg = String.format("parameter '%s' cannot be null", name);
            throw new IllegalArgumentException(msg);
        }
        return obj;
    }

    /**
     * argument validation
     *
     * @param expr expression
     * @param err  error message
     */
    public static void checkArg(boolean expr, String err) {
        if (!expr) {
            throw new IllegalArgumentException(err);
        }
    }

    /**
     * state validation
     *
     * @param expr expression
     * @param err  error message
     */
    public static void state(boolean expr, String err) {
        if (!expr) {
            throw new IllegalStateException(err);
        }
    }
}
