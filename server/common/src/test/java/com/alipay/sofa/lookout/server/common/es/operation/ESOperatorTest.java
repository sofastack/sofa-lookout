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
///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alipay.sofa.lookout.server.common.es.operation;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.io.IOException;
//
///**
// * @author: kevin.luy@antfin.com
// * @create: 2019-05-12 17:20
// **/
//public class ESOperatorTest {
//    String mapping = "{\"properties\": {\"id\": {\"type\": \"keyword\"},\"tags\": {\"type\": \"keyword\"},\"time\": {\"type\": \"date\"},\"value\": {\"type\": \"float\"}}}";
//
//    @Test
//    public void testBuildIndex() {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC)
//            .httpHost("http://localhost:9200").index("metrics").mapping("metrics", mapping).build();
//
//        esOperator.run();
//
//    }
//
//    @Test
//    public void testAutoScanTask() {
//
//    }
//
//    @Test
//    public void testIsAliasExisted() throws IOException {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        esOperator.isAliasExisted("lookout-active-metrics");
//    }
//
//    @Test
//    public void testIsIndexOrAliasExisted() throws IOException {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        Assert.assertTrue(esOperator.isIndexOrAliasExisted("lookout-active-metrics"));
//        Assert.assertFalse(esOperator.isIndexOrAliasExisted("lookout-active-metrics2"));
//    }
//
//    @Test
//    public void testCreateAlias() throws IOException {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        esOperator.doCreateIndexTemplate("active-metrics", "search-metrics", "metrics");
//
//    }
//
//    @Test
//    public void testCreateFirstIndex() throws IOException {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        esOperator.doCreateFirstIndex("metrics");
//    }
//
//    @Test
//    public void testAddMapping() throws IOException {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        boolean flag = esOperator.doCreateMapping("active-metrics", "metrics", mapping);
//        Assert.assertTrue(flag);
//    }
//
//    @Test
//    public void testDoRollover() {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        esOperator.doRollOver("lookout-active-metrics");
//    }
//
//    @Test
//    public void testDoDelete() {
//        ESOperator esOperator = new ESOperatorBuilder(ESDataType.METRIC).httpHost(
//            "http://localhost:9200").build();
//        esOperator.doDelete("metrics-2019.05.11-1");
//    }
//
//}
