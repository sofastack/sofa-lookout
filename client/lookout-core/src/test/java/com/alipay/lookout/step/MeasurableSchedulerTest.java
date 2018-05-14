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
package com.alipay.lookout.step;

import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kevin.luy@alipay.com on 2018/4/3.
 */
public class MeasurableSchedulerTest {
    private static final AtomicLong AL = new AtomicLong(0);

    @Test
    public void testSchedulerRunFixedRateSkipIfLong() {
        Registry r = new DefaultRegistry();
        MeasurableScheduler scheduler = new MeasurableScheduler(r, "fixed-rate", 1);
        AL.set(0);
        final AtomicLong latecount = new AtomicLong(0);
        scheduler.scheduleAtFixedRateSkipIfLong(new Runnable() {
            @Override
            public void run() {
                AL.incrementAndGet();
                Random r = new Random();
                int sleep = r.nextInt(150);
                if (sleep >= 100)
                    latecount.incrementAndGet();
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    System.err.println("eeee");
                }
                System.out.println("--sleep:" + sleep + "--now:" + System.currentTimeMillis());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduler.shutdown();
        String str = printMetrics(r);
        System.out.println("_____________" + AL.get());
    }

    @Test
    public void testSchedulerRunFixedRate() {
        Registry r = new DefaultRegistry();
        MeasurableScheduler scheduler = new MeasurableScheduler(r, "fixed-rate", 1);
        AL.set(0);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                AL.incrementAndGet();
                Random r = new Random();
                int sleep = r.nextInt(150);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    System.err.println("eeee");
                }
                System.out.println("--sleep:" + sleep + "--now:" + System.currentTimeMillis());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduler.shutdown();
        String str = printMetrics(r);
        System.out.println("_____________" + AL.get());
    }

    private String printMetrics(Registry r) {
        Iterator<Metric> it = r.iterator();
        String str = "";
        while (it.hasNext()) {
            str += it.next().measure().measurements();
        }
        System.out.println(str);
        return str;
    }

    @Test
    public void testSchedulerRunFixedDelay() {
        Registry r = new DefaultRegistry();
        MeasurableScheduler scheduler = new MeasurableScheduler(r, "fixed_delay", 1);
        AL.set(0);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                AL.incrementAndGet();
                Random r = new Random();
                int sleep = r.nextInt(150);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    System.err.println("eeee");
                }
                System.out.println("--sleep:" + sleep + "--now:" + System.currentTimeMillis());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduler.shutdown();
        printMetrics(r);
        System.out.println("_____________" + AL.get());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
