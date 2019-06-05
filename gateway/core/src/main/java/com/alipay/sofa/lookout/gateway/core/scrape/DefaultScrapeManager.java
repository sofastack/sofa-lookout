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
package com.alipay.sofa.lookout.gateway.core.scrape;

import com.alipay.sofa.lookout.gateway.core.common.MonitorComponent;
import com.alipay.sofa.lookout.gateway.core.scrape.config.ScrapeConfig;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * jobprocessor 执行任务；jobstatus记录最近状态；
 *
 * @author: kevin.luy@antfin.com
 * @create: 2019-01-03 18:23
 **/
public class DefaultScrapeManager implements ScrapeManager {
    private static final Logger log = LoggerFactory.getLogger(DefaultScrapeManager.class);
    ScheduledExecutorService scheduledExecutorService;
    private final MonitorComponent monitorComponentName;

    private final JobProcessor jobProcessor;
    private final JobConfigResolver jobConfigResolver;
    private final JobBuilder jobBuilder;
    private final Map<String, Long> fileFreshMarker = new ConcurrentHashMap<>();


    public DefaultScrapeManager(MonitorComponent monitorComponent, JobConfigResolver jobConfigResolver, JobBuilder jobBuilder) {
        this(monitorComponent, new JobProcessor(monitorComponent), jobConfigResolver, jobBuilder);
    }

    public DefaultScrapeManager(MonitorComponent monitorComponentName, JobProcessor jobProcessor, JobConfigResolver jobConfigResolver, JobBuilder jobBuilder) {
        Preconditions.checkArgument(monitorComponentName != null, "monitorComponentName is required!");
        this.jobProcessor = jobProcessor;
        this.jobConfigResolver = jobConfigResolver;
        this.jobBuilder = jobBuilder;
        this.monitorComponentName = monitorComponentName;
    }

    public JobProcessor getJobProcessor() {
        return jobProcessor;
    }

    @Override
    public List<ScrapeConfig> loadConfigFile(String configFile) throws FileNotFoundException {
        File file = ResourceUtils.getFile(configFile);
        return loadConfigFile(file);
    }

    public List<ScrapeConfig> loadConfigFile(File file) throws FileNotFoundException {
        Preconditions.checkArgument(file.exists() && file.isFile(), "invalid file:" + file.getPath());
        // 该文件是否过期
        Long lastModifiedTime = fileFreshMarker.get(file.getAbsolutePath());
        if (lastModifiedTime != null && file.lastModified() == lastModifiedTime) {
            return Collections.emptyList();
        }
        //read yaml
        Yaml yaml = new Yaml();
        Map<String, Object> configsMap = yaml.load(new FileInputStream(file));
        //resolve
        List<ScrapeConfig> scrapeConfigs = jobConfigResolver.resolve(file.getName(), file.lastModified(), configsMap);
        if (scrapeConfigs != null) {
            fileFreshMarker.put(file.getAbsolutePath(), file.lastModified());
        }
        return scrapeConfigs;
    }

    public List<ScrapeConfig> loadConfigFileFromFilePath(String dir) throws FileNotFoundException {
        List<ScrapeConfig> configs = new ArrayList<>();
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            return configs;
        }

        Preconditions.checkArgument(fileDir.isDirectory(), "invalid  directory: " + dir + " !");
        for (File file : fileDir.listFiles()) {
            if (!file.isFile()) {
                continue;
            }
            configs.addAll(loadConfigFile(file));
        }
        return configs;
    }

    @Override
    public synchronized ScheduledFuture<?> watchFreshScrapeConfigs(Runnable resolveFunction, long initialDelay, long period, TimeUnit unit) {
        if (scheduledExecutorService == null) {
            ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat(monitorComponentName + "-scrape-conf-watcher-%d").build();
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(tf);
        }
        return scheduledExecutorService.scheduleAtFixedRate(resolveFunction, initialDelay, period, unit);
    }

    @Override
    public synchronized <T extends ScrapeConfig> void updateScrapeConfigs(List<T> scrapeConfigs) {
        if (scrapeConfigs == null || scrapeConfigs.isEmpty()) {
            return;
        }

        for (T config : scrapeConfigs) {
            ScrapeJob job = jobProcessor.getRunnings().get(config.getJobName());
            if (job != null && job.getConfig() != null) {
                if (job.getConfig().getLastModifiedTime() >= config.getLastModifiedTime()) {
                    continue;
                } else {
                    //存在需要更新的 scrapeConfig，那么先停止老的;
                    log.info("update {} the scrape config:{}", monitorComponentName, job.getConfig());
                    job.stop();
                }
            }
            jobProcessor.execute(jobBuilder.build(config));
        }
    }
}
