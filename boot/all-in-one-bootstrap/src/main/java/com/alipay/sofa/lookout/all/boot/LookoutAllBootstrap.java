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
package com.alipay.sofa.lookout.all.boot;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.support.startup.SofaArkBootstrap;
import com.alipay.sofa.lookout.ark.support.BootUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author xiangfeng.xzc
 * @date 2019/5/20
 */
public class LookoutAllBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(LookoutAllBootstrap.class);

    public static void main(String[] args) throws Exception {
        init("lookout-all-boot");
        try {
            SofaArkBootstrap.launch(args);
        } catch (Exception e) {
            LOGGER.error("fail to start lookout all", e);
            throw e;
        }
    }

    private static void init(String bootName) throws IOException {
        if (System.getProperty(BootUtils.SOFA_ARK_MARK) != null) {
            return;
        }
        System.setProperty(BootUtils.SOFA_ARK_MARK, "true");

        copyConfigFiles(bootName);
    }

    /**
     * copy configs from classpath to external fileSystem
     *
     * @param bootName
     * @throws IOException
     */
    private static void copyConfigFiles(String bootName) throws IOException {
        Path tempDirectory = Files.createTempDirectory(bootName + "-configs");
        JSONObject configs = new JSONObject();
        configs.put("configDir", tempDirectory.toAbsolutePath().toString());
        String configsStr = configs.toJSONString();
        System.setProperty("sofaark.configs", configsStr);
        LOGGER.info("set system property sofaark.configs = {}", configsStr);

        // We now use ResourcePatternResolver from spring-core to help us find all properties in a directory
        Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(
            new ClassRelativeResourceLoader(LookoutAllBootstrap.class)).getResources(
            "classpath:app-configs/*/*.properties");
        for (Resource resource : resources) {
            String uri = resource.getURI().toString();
            int slashIndex1 = uri.lastIndexOf('/');
            int slashIndex0 = uri.lastIndexOf('/', slashIndex1 - 1);
            String app = uri.substring(slashIndex0 + 1, slashIndex1);
            String configName = uri.substring(slashIndex1 + 1);

            Path appConfigPath = tempDirectory.resolve(app);
            appConfigPath.toFile().mkdirs();
            Path targetPath = appConfigPath.resolve(configName);
            LOGGER.info("copy {} to {}", uri, targetPath);
            Files.copy(resource.getInputStream(), targetPath);
        }
    }
}
