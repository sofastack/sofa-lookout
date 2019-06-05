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
package com.alipay.sofa.lookout.server.interfaces;

import com.alipay.sofa.lookout.server.prom.ql.engine.PromQLEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2018/4/2.
 */
@RestController("indexController")
public class IndexController {

    @Value("${index.links.alert:}")
    private String       alertLink;
    @Value("${index.links.deer:}")
    private String       deerLink;
    @Value("${index.links.data_ingest:}")
    private String       dataIngestLink;

    private PromQLEngine engine;

    public IndexController(PromQLEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/_cat/health")
    public Boolean checkHealth() {
        return engine.getStorage().isHealthy();
    }

    @GetMapping("/index/entries")
    public Map<String, String> getIndexEntries() {
        Map<String, String> entries = new HashMap<>();
        entries.put("alert", alertLink);
        entries.put("deer", deerLink);
        entries.put("data_ingest", dataIngestLink);
        entries.put("doc", "https://sofa.alipay.com/lookout");
        return entries;
    }
}
