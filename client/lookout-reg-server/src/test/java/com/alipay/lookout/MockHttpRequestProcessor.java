package com.alipay.lookout;

import com.alipay.lookout.remote.report.support.http.HttpRequestProcessor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.Map;

/**
 * Created by kevin.luy@alipay.com on 2018/6/5.
 */
public class MockHttpRequestProcessor implements HttpRequestProcessor {

    public Object request;

    @Override
    public void sendPostRequest(HttpPost httpPost, Map<String, String> metadata) throws IOException {
        this.request = httpPost;
    }

    @Override
    public void sendGetRequest(HttpGet httpGet, Map<String, String> metadata) throws IOException {

    }
}
