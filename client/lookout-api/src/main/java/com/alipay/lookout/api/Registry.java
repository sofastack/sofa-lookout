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

import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.lookout.api.info.Info;

import java.util.Iterator;
import java.util.Map;

/**
 * manage a set of metrics.
 * Created by kevin.luy@alipay.com on 2017/2/14.
 */
public interface Registry extends Iterable<Metric> {

    /**
     * The clock used by the registry for timing events.
     *
     * @return clock
     */
    Clock clock();

    /**
     * Generate an identifier for a metric.
     *
     * @param name the name of an metric identifier.
     * @return id
     */
    Id createId(String name);

    /**
     * Generate an identifier for a metric.
     *
     * @param name the name of an metric identifier.
     * @param tags the tags of an metric identifier.
     * @return id
     */
    Id createId(String name, Iterable<Tag> tags);

    Id createId(String name, Map<String, String> tags);

    /**
     * Add a custom metric to the registry.
     * <p>
     * caution: if the same id is already existed,then throw duplicate exception
     *
     * @param metric metric
     */
    void register(Metric metric);

    /**
     * Register a gauge instance
     *
     * @param id    metric id
     * @param gauge gauge
     * @param <T>   return old one;if this id first register gauge ,return null
     * @return gauge
     */
    <T extends Number> Gauge<T> gauge(Id id, Gauge<T> gauge);

    /**
     *  Remove a metric by id.
     * @param id metric id
     */
    void removeMetric(Id id);

    /**
     * Register a info instance
     * @param id id metric id
     * @param info an info instance
     * @param <I> info data
     * @param <Y> info type
     * @return info info
     */
    <I, Y extends Info<I>> Info info(Id id, Y info);

    /**
     * Measures the rate of some activity. A counter is for continuously incrementing sources like
     * the number of requests that are coming into a server.
     *
     * @param id metric id
     * @return counter
     */
    Counter counter(Id id);

    /**
     * Measures the rate and variation in amount for some activity. For example, it could be used to
     * get insight into the variation in response sizes for requests to a server.
     *
     * @param id metric id
     * @return DistributionSummary
     */
    DistributionSummary distributionSummary(Id id);

    /**
     * Measures the rate and time taken for short running tasks.
     *
     * @param id metric id
     * @return timer
     */
    Timer timer(Id id);

    /**
     * Generate a mixinMetric
     *
     * @param id metric id
     * @return a mixinMetric instance
     */
    MixinMetric mixinMetric(Id id);

    /**
     * Returns the metric associated with a given id.
     * <p>
     * if reaching the max number,null will be returned too.
     *
     * @param id metric id
     * @param <X> metric type
     * @return X Instance of the metric or null if there is no match.
     */
    <X extends Metric> X get(Id id);

    /**
     * Iterator for traversing the set of metrics in the registry.
     */
    Iterator<Metric> iterator();

    /**
     * propagate a exception
     *
     * @param msg the exception message
     * @param t   the exception
     */
    void propagate(String msg, Throwable t);
}
