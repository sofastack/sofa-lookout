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
package com.alipay.sofa.lookout.server.prom.ql.func.support;

/**
 * @author zhangzhuo
 * @version $Id: Bucket.java, v 0.1 2018年09月10日 下午4:25 zhangzhuo Exp $
 */
public class Bucket {

    private long lowerBound;

    private long upperBound;

    private long count;

    private long sum;

    public Bucket(long lowerBound, long upperBound, long count) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.count = count;
    }

    public long getLowerBound() {
        return lowerBound;
    }

    public long getUpperBound() {
        return upperBound;
    }

    public long getCount() {
        return count;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }
}