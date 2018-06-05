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
package com.alipay.lookout.remote.report.support;

import com.alipay.lookout.remote.report.support.http.DefaultHttpRequestProcessor;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static com.alipay.lookout.remote.report.support.http.DefaultHttpRequestProcessor.WAIT_MINUTES;

/**
 * Created by kevin.luy@alipay.com on 2017/4/13.
 */
public class HttpRequestProcessorTest {
    final ReportDecider        reportDecider        = new ReportDecider();
    final DefaultHttpRequestProcessor httpRequestProcessor = new DefaultHttpRequestProcessor(reportDecider);

    @Test
    public void testHandleErrorResponse401() {
        Assert.assertFalse(reportDecider.stillSilent());
        httpRequestProcessor.handleErrorResponse(mockHttpResponse(401));
        Assert.assertTrue(reportDecider.stillSilent());
    }

    private HttpResponse mockHttpResponse(int status) {

        HttpResponse response = Mockito.mock(HttpResponse.class);
        Header mockheader = Mockito.mock(Header.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);

        Mockito.when(response.containsHeader(WAIT_MINUTES)).thenReturn(true);

        Mockito.when(response.getFirstHeader(WAIT_MINUTES)).thenReturn(mockheader);
        Mockito.when(mockheader.getValue()).thenReturn("1");

        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(status);
        return response;
    }
}
