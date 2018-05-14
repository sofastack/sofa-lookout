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
//package com.alipay.lookout.remote.report;
//
//import LookoutConfig;
//import LookoutConfig;
//import com.alipay.lookout.remote.model.LookoutMeasurement;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
///**
// * Created by kevin.luy@alipay.com on 2017/3/15.
// */
//public class HttpObserverTest {
//    static HttpObserver observer;
//    static String msg;
//
//    @BeforeClass
//    public static void init() {
//        LookoutConfig lookoutConfig = new LookoutConfig();
//        observer = new HttpObserver(lookoutConfig);
//
//        List<LookoutMeasurement> measurements = new ArrayList<LookoutMeasurement>();
//        for (int i = 0; i < 20000; i++) {
//            LookoutMeasurement measurement = new LookoutMeasurement(new Date());
//            measurement.addTag("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxx");
//            measurement.addTag("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxx");
//            measurement.addTag("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxx");
//            measurement.addTag("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxx");
//            measurement.addTag("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "xxxxxxxxxxxx");
//            measurement.put("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "0");
//            measurements.add(measurement);
//        }
//        msg = observer.buildReportText(measurements);
//    }
//
//    @Test
//    public void testReport() {
//        for (int i = 0; i < 3; i++) {
//            long s = System.currentTimeMillis();
//            observer.report2Agent("localhost", msg);
//            System.out.println(msg.length() + "==str===>" + (System.currentTimeMillis() - s));
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Test
//    public void testReportSnappy() {
//        for (int i = 0; i < 3; i++) {
//            long s = System.currentTimeMillis();
//            observer.reportSnappy2Agent("localhost", msg);
//            System.out.println(msg.length() + "==Snappy===>" + (System.currentTimeMillis() - s));
////            try {
////                Thread.sleep(20000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//        }
//    }
//
//    @Test
//    public void testReportGzip() {
//        for (int i = 0; i < 3; i++) {
//            long s = System.currentTimeMillis();
//            observer.reportGzip2Agent("localhost", msg);
//            System.out.println(msg.length() + "==gzip===>" + (System.currentTimeMillis() - s));
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//}
