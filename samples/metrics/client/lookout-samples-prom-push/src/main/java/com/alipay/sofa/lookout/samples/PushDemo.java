package com.alipay.sofa.lookout.samples;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.PushGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: kevin.luy@antfin.com
 * @create: 2019-05-30 17:54
 **/
@SpringBootApplication
public class PushDemo {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(PushDemo.class, args);
        executeBatchJob();
    }

    static void executeBatchJob() throws Exception {
        CollectorRegistry registry = CollectorRegistry.defaultRegistry;
        Counter requests = Counter.build()
                .name("my_library_requests_total").help("Total requests.")
                .labelNames("method").register();
        requests.labels("get").inc();


        PushGateway pushgateway = new PushGateway("127.0.0.1:7200/prom");
        // pushgateway.setConnectionFactory(new BasicAuthHttpConnectionFactory("my_user", "my_password"));
        Map<String, String> groupingkeys = new HashMap<>();
        groupingkeys.put("app", "xx");
        pushgateway.pushAdd(registry, "my_batch_job", groupingkeys);
        //  pushgateway.pushAdd(registry, "my_batch_job");
    }
}
