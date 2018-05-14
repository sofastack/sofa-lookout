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
package com.alipay.lookout.reg.prometheus;

import com.alipay.lookout.reg.prometheus.exporter.ExporterServer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by kevin.luy@alipay.com on 2018/5/10.
 */
public class ExporterServerTest {

    @Test
    public void testExporterServer() throws IOException {
        ExporterServer server = new ExporterServer(9494);
        server.start();
        String result = sendHttpRequest(new URL("http://localhost:9494/"));
        Assert.assertTrue(result.contains("/metrics"));
        server.stop();
    }

    public static String sendHttpRequest(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET"); //使用get请求
        conn.connect();
        String length = conn.getHeaderField("Content-Length");
        byte[] bytes = new byte[Integer.valueOf(length)];
        IOUtils.readFully(conn.getInputStream(), bytes);
        return new String(bytes, Charset.forName("utf-8"));
    }
}
