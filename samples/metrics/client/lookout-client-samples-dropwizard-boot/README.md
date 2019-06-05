## 这里演示基于 dropwizard metrics 为基础注册表与 SpringBoot Actuator 集成

- 添加依赖

```
<dependency>
    <groupId>com.alipay.sofa.lookout</groupId>
    <artifactId>lookout-reg-dropwizard</artifactId>
</dependency>
<dependency>
    <groupId>io.dropwizard.metrics</groupId>
    <artifactId>metrics-core</artifactId>
</dependency>
```

- 启动后访问：http://localhost:8080/metrics

```
...
http_requests_total.instant-luyideMacBook-Pro.local: 1,
...
```