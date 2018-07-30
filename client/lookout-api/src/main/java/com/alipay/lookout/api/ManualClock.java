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

/**
 * 一个可以手动调整的Clock实现, 常用于单元测试
 *
 * @author xiangfeng.xzc
 * @date 2018/7/27
 */
public class ManualClock implements Clock {
    private volatile long wallTime;
    private volatile long monotonicTime;

    @Override
    public long wallTime() {
        return wallTime;
    }

    @Override
    public long monotonicTime() {
        return monotonicTime;
    }

    public void setWallTime(long wallTime) {
        this.wallTime = wallTime;
        this.monotonicTime = wallTime * 1000L;
    }

    public void setMonotonicTime(long monotonicTime) {
        this.wallTime = monotonicTime / 1000L;
        this.monotonicTime = monotonicTime;
    }
}
