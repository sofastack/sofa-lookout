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
//package com.alipay.lookout.remote.step;
//
//import com.alipay.lookout.api.NoopRegistry;
//import com.alipay.lookout.step.Scheduler;
//import org.junit.Test;
//
//import java.util.concurrent.Future;
//
///**
// * Created by kevin.luy@alipay.com on 2017/2/23.
// */
//public class SchedulerMultiTaskTest {
//
//    @Test
//    public void testMultiTaskAndCancleOne() {
//        Scheduler.Options options = new Scheduler.Options()
//                .withFrequency(Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, 1000)
//                .withInitialDelay(100)
//                .withStopOnFailure(false);
//
//        Scheduler scheduler = new Scheduler(NoopRegistry.INSTANCE, "poller", 3);
//        Future future1 =   scheduler.schedule(options, new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("111111");
//            }
//        });
//
//       Scheduler.Options options2 = new Scheduler.Options()
//                .withFrequency(Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, 2000)
//                .withInitialDelay(100)
//                .withStopOnFailure(false);
//
//        Future future2 = scheduler.schedule(options2, new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("222222");
//            }
//        });
//
//        future1.cancel(false);
//        System.out.println("==========");
//
//
//        try {
//            Thread.sleep(20000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
