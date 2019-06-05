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
package com.alipay.sofa.lookout.gateway.core.prototype.filter;

import java.util.Map;
import java.util.function.Predicate;

/**
 * 过滤器用于剔除非法数据, 但过滤器不一定是用来剔除非法数据, 也可以用来修改数据
 *
 * @author xiangfeng.xzc
 * @date 2018/11/16
 */
public interface Filter<T> {
    /**
     * 如果过滤的结果是保留该数据, 请返回该对象, 其他对象一律视作过滤失败, 另外采用类似的方式合理cached住返回值可以提高一点性能
     */
    FilterResult SUCCESS = new FilterResult("success");
    FilterResult FAIL    = new FilterResult("fail");

    /**
     * 该过滤器的名字
     *
     * @return
     */
    String name();

    /**
     * event,trace,metric,log. 有一些filter是通用的, 可以用any.
     *
     * @return
     */
    String type();

    /**
     * 对数据进行过滤, 如果通过过滤, 就返回 SUCCESS, 否则返回失败原因(一个字符串)
     *
     * @param t
     * @param filterContext
     * @return
     */
    FilterResult test(T t, Map<String, ?> filterContext);

    default Predicate<T> asPredicate(Map<String, ?> filterContext) {
        return t -> test(t, filterContext) == SUCCESS;
    }

    class FilterResult {
        private final String msg;

        public FilterResult(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }
    }
}
