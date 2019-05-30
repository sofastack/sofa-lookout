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
package com.alipay.sofa.lookout.server.starter;

import com.alipay.sofa.lookout.server.storage.ext.es.spring.bean.config.ElasticSearchServerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by kevin.luy@alipay.com on 2018/3/23.
 */
@Configuration
@Import({ ElasticSearchServerConfig.class })
@ComponentScan("com.alipay.sofa.lookout.server.interfaces")
@EnableConfigurationProperties(ServerProperties.class)
public class ServerAutoConfiguration {

    public ServerAutoConfiguration(ServerProperties serverProperties) {
        Assert.notNull(serverProperties.getStorage(),
            "No metrics server storage configured[\"metrics.server.storage=?\"]!");
    }

    /**
     * for webmvc
     *
     * @return
     */
    @Bean
    public WebMvcConfigurer forwardToIndex() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // forward requests to /admin and /user to their index.html
                registry.addViewController("/").setViewName("redirect:/index.html");
            }
        };
    }
}
