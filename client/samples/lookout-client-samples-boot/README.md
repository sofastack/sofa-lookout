## 这里演示与 SpringBoot Actuator 集成,以 lookout 作为 metrics 注册表；

- 添加依赖

```
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
 </dependency>
```

- 启动后访问：http://localhost:8080/metrics

```
...
http_requests_total.instant-luyideMacBook-Pro.local: 1,
...
```