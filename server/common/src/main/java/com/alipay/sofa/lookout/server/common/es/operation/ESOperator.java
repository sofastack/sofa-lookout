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
package com.alipay.sofa.lookout.server.common.es.operation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-12 17:09
 **/
public class ESOperator {
    private static final Logger logger = LoggerFactory.getLogger(ESOperator.class);

    private ESOperatorBuilder   esOperatorBuilder;
    private final OkHttpClient  client = new OkHttpClient();

    ESOperator(ESOperatorBuilder esOperatorBuilder) {
        this.esOperatorBuilder = esOperatorBuilder;
    }

    public void run() {
        if (!esOperatorBuilder.autoScanEnable) {
            return;
        }
        // add scheduler tasks
        esOperatorBuilder.getScheduler().scheduleAtFixedRate(() -> {
            doRollOver(esOperatorBuilder.alias);
        }, 0, 10, TimeUnit.MINUTES);
        esOperatorBuilder.getScheduler().scheduleAtFixedRate(() -> {
            doDelete(esOperatorBuilder.index);
        }, 0, 10, TimeUnit.MINUTES);
    }

    public void initializeDatabase() {
        final String alias = esOperatorBuilder.alias;
        Preconditions.checkNotNull(alias,
            String.format("no es alias found for %s !", esOperatorBuilder.dataType));

        try {
            if (!isAliasExisted(alias)) {
                logger.info("ElasticSearch operator initializes the {} storage",
                    esOperatorBuilder.dataType);
                createIndex();
                if (esOperatorBuilder.mappingType != null) {
                    doCreateMapping(esOperatorBuilder.alias, esOperatorBuilder.mappingType,
                        esOperatorBuilder.mappingContent);
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    void createIndex() throws IOException {
        String index = esOperatorBuilder.index;
        String alias = esOperatorBuilder.alias;
        String searchAlias = esOperatorBuilder.searchAlias;
        //        //no alias, but check whether any index name conflicts with the alias.
        Preconditions.checkState(!isIndexOrAliasExisted(alias),
            "this alias is conflicted with an index name:" + alias);
        //create alias and indices
        doCreateIndexTemplate(alias, searchAlias, index);
        String realIndex = doCreateFirstIndex(index);
        if (StringUtils.isNotEmpty(realIndex)) {
            doCreateIndexAlias(realIndex, alias);
        } else {
            logger.error("can't get the first index name");
        }
    }

    boolean isIndexOrAliasExisted(String index) throws IOException {
        final Request request = new Request.Builder().url(esOperatorBuilder.hostUrl + "/" + index)
            .build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    boolean isAliasExisted(String alias) throws IOException {
        final Request request = new Request.Builder().url(
            esOperatorBuilder.hostUrl + "/_cat/aliases").build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string().contains(alias + " ");
        }
        return false;
    }

    int doGetClusterNodesNum() throws IOException {
        final Request request = new Request.Builder()
            .url(esOperatorBuilder.hostUrl + "/_cat/nodes").build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String[] nodes = StringUtils.split(response.body().string(), "\n");
            return nodes.length;
        }
        return 1;
    }

    boolean doCreateIndexTemplate(String alias, String searchAlias, String index)
                                                                                 throws IOException {
        String aliasTemplate = "{\"template\": \"%s-*\",\"settings\": {\"number_of_shards\":3,\"number_of_replicas\": %d,\"routing.allocation"
                               + ".total_shards_per_node\": 9},\"aliases\": {\"%s\": {}}}";
        int nodesNum = doGetClusterNodesNum();
        String body = String.format(aliasTemplate, index, nodesNum > 1 ? 1 : 0, searchAlias);
        logger.info("index template:{}", body);
        final Request request = new Request.Builder()
            .url(esOperatorBuilder.hostUrl + String.format("/_template/%s", alias + "-t"))
            .put(RequestBody.create(MediaType.parse("application/json"), body.getBytes("UTF-8")))
            .build();
        logger.info("create template:{}", request.toString());
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    boolean doCreateIndexAlias(String index, String alias) {
        String addAliasTemplate = "{\"actions\":[{\"add\":{\"index\":\"%s\",\"alias\":\"%s\"}}]}";
        String addAlias = String.format(addAliasTemplate, index, alias);
        try {
            final Request request = new Request.Builder()
                .url(esOperatorBuilder.hostUrl + "/_aliases")
                .post(
                    RequestBody.create(MediaType.parse("application/json"),
                        addAlias.getBytes("UTF-8"))).build();
            Response response = client.newCall(request).execute();
            logger.info("add alias:{}", addAlias);
            if (!response.isSuccessful()) {
                logger.warn("add alias fail!", response.body().string());
            }
        } catch (Throwable e) {
            logger.warn("add alias fail!", e.getMessage());
        }
        return true;
    }

    /**
     *
     * @param index
     * @return the created real index name
     * @throws IOException
     */
    String doCreateFirstIndex(String index) throws IOException {
        String url = URLEncoder.encode(String.format("<%s-{now/d}-1>", index), "UTF-8");
        final Request request = new Request.Builder().url(esOperatorBuilder.hostUrl + "/" + url)
            .put(RequestBody.create(MediaType.parse("application/json"), "".getBytes("UTF-8")))
            .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return getIndexNameFromResponse(response.body().string());
        } else {
            return getIndexNameFromErrorResponse(response.body().string());
        }
    }

    boolean doCreateMapping(String alias, String type, String mapping) throws IOException {
        final Request request = new Request.Builder()
            .url(esOperatorBuilder.hostUrl + "/" + String.format("%s/%s/_mapping", alias, type))
            .put(RequestBody.create(MediaType.parse("application/json"), mapping.getBytes("UTF-8")))
            .build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }

    void doRollOver(String alias) {
        String rollOverContent = String.format(
            "{\"conditions\": {\"max_age\": \"%s\",\"max_docs\": %d}}", esOperatorBuilder.maxAge,
            esOperatorBuilder.maxDocs);
        try {
            final Request request = new Request.Builder()
                .url(esOperatorBuilder.hostUrl + "/" + alias + "/_rollover")
                .post(
                    RequestBody.create(MediaType.parse("application/json"),
                        rollOverContent.getBytes("UTF-8"))).build();
            Response response = client.newCall(request).execute();
            if (logger.isDebugEnabled()) {
                logger.debug("rollOver:{}", request.toString());
            }
            if (!response.isSuccessful()) {
                logger.warn("rollOver fail!", response.body().string());
            }
        } catch (Throwable e) {
            logger.warn("rollOver fail!", e.getMessage());
        }
    }

    void doDelete(String index) {
        final String INDEX_TEMPLATE = index + "-%s%s";
        String idx = null;
        try {
            long validDays = esOperatorBuilder.validDays;
            LocalDate todayKolkata = LocalDate.now(ZoneId.of("Asia/Shanghai"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            idx = String.format(INDEX_TEMPLATE,
                todayKolkata.minusDays(validDays).format(formatter), "*");

            final Request request = new Request.Builder()
                .url(esOperatorBuilder.hostUrl + "/" + idx).delete().build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.warn("delete the old metrics index:{} fail.", idx);
            }
            logger.debug("delete the old metrics index:{} fail.", idx);
        } catch (Throwable e) {
            logger.warn("delete the old metrics index:{} fail. err:{}", idx, e.getMessage());
        }
    }

    private String getIndexNameFromResponse(String response) {
        try {
            JSONObject resJSON = JSON.parseObject(response);
            return resJSON.getString("index");
        } catch (Exception e) {
            logger.warn("parse create index response error.", e);
            return null;
        }
    }

    private String getIndexNameFromErrorResponse(String response) {
        try {
            JSONObject resJSON = JSON.parseObject(response);
            if (resJSON.containsKey("error")) {
                JSONObject errorJSON = resJSON.getJSONObject("error");
                return errorJSON.getString("index");
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warn("parse create index response error.", e);
            return null;
        }
    }

}
