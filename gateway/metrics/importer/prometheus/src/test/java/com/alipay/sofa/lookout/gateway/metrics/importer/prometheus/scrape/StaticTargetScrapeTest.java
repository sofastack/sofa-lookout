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
package com.alipay.sofa.lookout.gateway.metrics.importer.prometheus.scrape;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.scrape.*;
import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;
import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-04 12:01
 **/
public class StaticTargetScrapeTest {
    static HttpServer server;

    @BeforeClass
    public static void init() throws IOException {
        server = HttpServer.create(new InetSocketAddress(1234), 0);
        server.createContext("/metrics", t -> {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @AfterClass
    public static void destory() {
        server.stop(1);
    }

    /**
     * 任务解析和运行正常，但是任务抓取对应服务请求失败;
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testStaticTargetConfig() throws FileNotFoundException {
        JobConfigResolver jobConfigResolver = new PromJobConfigResolver();
        JobBuilder jobBuilder = new PromScrapeJobBuilder();
        ScrapeManager scrapeManager = new DefaultScrapeManager(MonitorComponent.METRIC,
            jobConfigResolver, jobBuilder);
        List<ScrapeConfig> scrapeConfigs = scrapeManager
            .loadConfigFile("classpath:static_targets_config.yml");
        scrapeManager.updateScrapeConfigs(scrapeConfigs);
        ScrapeJob job = ((DefaultScrapeManager) scrapeManager).getJobProcessor().getRunnings()
            .get("prometheus");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(job.isRunning());
        JobState state = (JobState) job.getStates().get(0);
        Assert.assertTrue(state.isSuccessful());
    }

    /**
     * 先解析配置文件并运行，然后在更新新的配置再运行;
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testStaticTargetConfigAndReplaceJobConfig() throws FileNotFoundException {
        JobConfigResolver jobConfigResolver = new PromJobConfigResolver();
        JobBuilder jobBuilder = new PromScrapeJobBuilder();
        ScrapeManager scrapeManager = new DefaultScrapeManager(MonitorComponent.METRIC,
                jobConfigResolver, jobBuilder);
        List<ScrapeConfig> scrapeConfigs = scrapeManager.loadConfigFile("classpath:static_targets_config.yml");
        scrapeManager.updateScrapeConfigs(scrapeConfigs);
        ScrapeJob job = ((DefaultScrapeManager) scrapeManager).getJobProcessor().getRunnings().get("prometheus");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("===========refresh config==========");
        //new 配置
        StaticScrapeConfig config = new StaticScrapeConfig();
        config.setJobName("prometheus");
        config.setLastModifiedTime(System.currentTimeMillis());
        List<StaticScrapeConfig.StaticConfigItem> staticConfigItemList = new ArrayList<>();
        StaticScrapeConfig.StaticConfigItem item = new StaticScrapeConfig.StaticConfigItem();
        item.setTargets(Lists.newArrayList("localhost:8234"));
        staticConfigItemList.add(item);

        config.setStaticConfigItemList(staticConfigItemList);
        scrapeManager.updateScrapeConfigs(Lists.newArrayList(config));
        Assert.assertFalse(job.isRunning());
        ScrapeJob job2 = ((DefaultScrapeManager) scrapeManager).getJobProcessor().getRunnings().get("prometheus");
        Assert.assertTrue(job2.isRunning());


        Assert.assertTrue(job2.getStates().isEmpty());
    }

    @Test
    public void testUpdateStaticTargetConfigScheduleFromPath() throws FileNotFoundException {
        JobConfigResolver jobConfigResolver = new PromJobConfigResolver();
        JobBuilder jobBuilder = new PromScrapeJobBuilder();
        ScrapeManager scrapeManager = new DefaultScrapeManager(MonitorComponent.METRIC,
                jobConfigResolver, jobBuilder);
        File file = ResourceUtils.getFile("classpath:static_targets_config.yml");
        scrapeManager.watchFreshScrapeConfigs(() -> {
            try {
                scrapeManager.updateScrapeConfigs(scrapeManager.loadConfigFileFromFilePath(file.getParent()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            ScrapeJob job = ((DefaultScrapeManager) scrapeManager).getJobProcessor().getRunnings().get("prometheus");
            Assert.assertTrue(job.isRunning());
//            Assert.assertTrue(job.getState().isSuccessful());

        }, 0, 10, TimeUnit.SECONDS);


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
