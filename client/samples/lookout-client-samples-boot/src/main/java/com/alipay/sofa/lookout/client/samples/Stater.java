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
package com.alipay.sofa.lookout.client.samples;

import com.alipay.lookout.api.Counter;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.common.utils.NetworkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by kevin.luy@alipay.com on 2018/4/23.
 */
@Component
@ComponentScan
@SpringBootApplication
public class Stater {
    @Autowired
    private Registry registry;

    @PostConstruct
    public void init() {
        Counter counter = registry.counter(registry.createId("http_requests_total").withTag("instant", NetworkUtil.getLocalAddress().getHostName()));
        counter.inc();
    }

    public static void main(String[] args) {
        try {
            SpringApplication.run(Stater.class, args);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
}
