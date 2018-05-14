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

import com.alibaba.fastjson.JSON;
import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.lookout.remote.model.LookoutMeasurement;
import org.junit.Test;

import java.util.Date;

/**
 * Created by kevin.luy@alipay.com on 2018/3/28.
 */
public class JsonStringfyTest {

    //    @Test(expected = JSONException.class)
    @Test
    public void testMeasurmentStringWithJsonParseException() {
        Registry reg = new DefaultRegistry();
        Id id = reg.createId("test");
        LookoutMeasurement m = new LookoutMeasurement(new Date(), id);
        m.addTag(
            "params",
            "\"LAZADA_USD_CNY_PRICE;USD/CNY;SPOT;6.1111;null;null;20180328;2018-03-28 17:58:21.135;2018-03-26 11:09:19.517;null;null;null;0;0;0;0;20180328000400130000000001294460;null;null;null;TODAY;null;null;?????;N;EXCORENC;N;1;Y;{\"lastExecContraAmt\":\"3.06\",\"lastExecContraCcy\":\"CNY\",\"requestedRateStatus\":\"QUALIFY\",\"tntInstId\":\"GNETW7CN\",\"valueDate\":\"20180327\"};null;null;null;null;LAZADA_PRICE;null;null;\"\n");
        JSON.parse(m.toString());
    }

    //    @Test
    //    public void testMeasurmentStringEscapJsonStr() {
    //        Registry reg = new DefaultRegistry();
    //        Id id = reg.createId("test");
    //        LookoutMeasurement m = new LookoutMeasurement(new Date(), id);
    //        m.addTag("params", StringEscapeUtils.escapeJson("\"LAZADA_USD_CNY_PRICE;USD/CNY;SPOT;6.1111;null;null;20180328;2018-03-28 17:58:21.135;2018-03-26 11:09:19.517;null;null;null;0;0;0;0;20180328000400130000000001294460;null;null;null;TODAY;null;null;?????;N;EXCORENC;N;1;Y;{\"lastExecContraAmt\":\"3.06\",\"lastExecContraCcy\":\"CNY\",\"requestedRateStatus\":\"QUALIFY\",\"tntInstId\":\"GNETW7CN\",\"valueDate\":\"20180327\"};null;null;null;null;LAZADA_PRICE;null;null;\"\n"));
    //        JSON.parse(m.toString());
    //    }
}
