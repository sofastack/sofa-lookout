package com.alipay.lookout.remote.report.support.http;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2018/6/5.
 */
public interface HttpRequestProcessor {
    void sendPostRequest(HttpPost httpPost, Map<String, String> metadata) throws IOException;

    void sendGetRequest(HttpGet httpGet, Map<String, String> metadata) throws IOException;
}
