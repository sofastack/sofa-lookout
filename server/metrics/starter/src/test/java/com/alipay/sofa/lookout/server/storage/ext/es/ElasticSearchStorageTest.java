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
//package com.alipay.sofa.lookout.server.storage.ext.es;
//
//import com.alipay.sofa.lookout.server.prom.ql.engine.PromQLEngine;
//import com.alipay.sofa.lookout.server.prom.ql.engine.Query;
//import com.alipay.sofa.lookout.server.prom.ql.engine.Result;
//import com.alipay.sofa.lookout.server.starter.ServerAutoConfiguration;
//import com.alipay.sofa.lookout.server.storage.ext.es.spring.bean.config.ElasticSearchServerConfig;
//import com.github.vanroy.springboot.autoconfigure.data.jest.ElasticsearchJestAutoConfiguration;
//import com.github.vanroy.springboot.autoconfigure.data.jest.ElasticsearchJestDataAutoConfiguration;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.time.Duration;
//import java.time.Instant;
//
///**
// * @author: kevin.luy@antfin.com
// * @create: 2019-05-05 11:44
// **/
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {ElasticSearchStorageTest.class, ServerAutoConfiguration.class,
//        // JestAutoConfiguration.class,
//        ElasticSearchServerConfig.class,
//        ElasticsearchJestAutoConfiguration.class,
//        ElasticsearchJestDataAutoConfiguration.class}
//// , value = {"spring.data.jest.uri=http://localhost:9200"}
//)
//public class ElasticSearchStorageTest {
//    @Autowired
//    private PromQLEngine engine;
//
//    @Test
//    public void testRangeQuery() {
//        String query = "jvm.classes.total{app=\"lookoutgateway\"}";
//        Query qry = engine.newRangeQuery(query, Instant.ofEpochMilli(1557036880005l),
//                Instant.ofEpochMilli(1557037180005l), Duration.ofSeconds(Long.parseLong("15")));
//        qry.hints().setUseNative(false);
//        Result result = qry.exec();
//        System.out.println(result.toString());
//    }
//
//}
