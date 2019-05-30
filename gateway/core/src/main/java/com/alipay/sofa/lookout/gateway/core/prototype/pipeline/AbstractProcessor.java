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

import com.alipay.sofa.lookout.gateway.core.prototype.lifecycle.LifeCycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author xiangfeng.xzc
 * @date 2018/11/13
 */
public abstract class AbstractProcessor<I, O> extends LifeCycleSupport implements Processor<I, O> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final List<Consumer<O>> consumers = new ArrayList<>();

    @Override
    public Processor<I, O> consume(Consumer<O> listener) {
        consumers.add(listener);
        return this;
    }

    @Override
    public Processor<I, O> consume(Processor<O, ?> processor) {
        return this.consume(processor::onInput);
    }

    @Override
    public void onOutput(O o) {
        for (Consumer<O> listener : consumers) {
            listener.accept(o);
        }
    }

    @Override
    public <P> Processor<I, P> map(Function<O, P> mapper) {
        Processor<I, P> p = new NoInputProcessor<>();
        this.consume(o -> p.onOutput(mapper.apply(o)));
        return p;
    }

    @Override
    public <P> Processor<I, P> flatMap(BiConsumer<O, Consumer<P>> consumer) {
        Processor<I, P> p = new NoInputProcessor<>();
        this.consume(o -> consumer.accept(o, p::onOutput));
        return p;
    }

    @Override
    public Processor<I, O> filter(Predicate<O> predicate) {
        Processor<I, O> p = new NoInputProcessor<>();
        this.consume(o -> {
            if (predicate.test(o)) {
                p.onOutput(o);
            }
        });
        return p;
    }

    @Override
    public <P> Processor<O, P> then(Processor<O, P> next) {
        this.consume(next::onInput);
        return next;
    }

    @Override
    public Processor<I, O> visit(Consumer<Processor<I, O>> visitor) {
        visitor.accept(this);
        return this;
    }

    @Override
    public <II, OO> Processor<II, OO> compose(Function<Processor<I, O>, Processor<II, OO>> composer) {
        return composer.apply(this);
    }

    @Override
    public Processor<I, O> observeOn(Executor computingThreadPool) {
        Processor<I, O> p = new NoInputProcessor<>();
        this.consume(o -> computingThreadPool.execute(() -> {
            try {
                p.onOutput(o);
            } catch (Exception e) {
                LOGGER.warn("后续处理抛出异常", e);
            }
        }));
        return p;
    }
}
