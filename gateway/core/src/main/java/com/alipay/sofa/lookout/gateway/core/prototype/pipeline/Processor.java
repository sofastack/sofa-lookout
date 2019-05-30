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
package com.alipay.sofa.lookout.gateway.core.prototype.pipeline;

import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycle;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 一个Processor既是生产者也是消费者 TODO 是不是改成只支持一个消费者会比较好?
 *
 * @param <I> 输入类型
 * @param <O> 输出类型
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public interface Processor<I, O> extends LifeCycle {

    /**
     * 喂给这个处理器一个输入
     *
     * @param i
     */
    void onInput(I i);

    /**
     * 由外部触发该处理器产生, 即输出并不一定是由该处理器内部产生的, 外部可以调用这个方法使得好像是该处理器产生了数据一样
     *
     * @param o
     */
    void onOutput(O o);

    /**
     * 添加一个消费者, 注意消费者消费的数据类型是O
     *
     * @param consumer
     * @return 返回自己
     */
    Processor<I, O> consume(Consumer<O> consumer);

    /**
     * @param processor
     * @return 返回自己
     */
    Processor<I, O> consume(Processor<O, ?> processor);

    /**
     * 对输出进行map, 产生一个新的处理器, 注意泛型参数! 该处理器不接收输入, 只能通过处理器做输入
     *
     * @param mapper
     * @param <P>
     * @return
     */
    <P> Processor<I, P> map(Function<O, P> mapper);

    <P> Processor<I, P> flatMap(BiConsumer<O, Consumer<P>> consumer);

    /**
     * 对输出进行filter, 产生一个新的处理器, 注意泛型参数!
     *
     * @param predicate
     * @return
     */
    Processor<I, O> filter(Predicate<O> predicate);

    /**
     * 这是一个辅助函数, 相当于 this.consume(next::onInput); return next;
     *
     * @param next
     * @param <P>
     * @return
     */
    <P> Processor<O, P> then(Processor<O, P> next);

    /**
     * visit模式, 用于临时插入代码 保持流式API不中断
     *
     * @param visitor
     * @return
     */
    Processor<I, O> visit(Consumer<Processor<I, O>> visitor);

    /**
     * visit模式, 用于临时插入代码 保持流式API不中断, 允许转换Processor
     *
     * @param composer
     * @param <II>
     * @param <OO>
     * @return
     */
    <II, OO> Processor<II, OO> compose(Function<Processor<I, O>, Processor<II, OO>> composer);

    /**
     * 切换线程
     *
     * @param computingThreadPool
     * @return
     */
    Processor<I, O> observeOn(Executor computingThreadPool);
}
