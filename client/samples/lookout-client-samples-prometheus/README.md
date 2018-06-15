## 1.加入依赖

```xml
        <dependency>
            <groupId>com.alipay.sofa.lookout</groupId>
            <artifactId>lookout-sofa-boot-starter</artifactId>
            <version>${lookout.client.version}</version>
        </dependency>
        <dependency>
            <groupId>com.alipay.sofa.lookout</groupId>
            <artifactId>lookout-reg-prometheus</artifactId>
            <version>${lookout.client.version}</version>
        </dependency>
```

## 2.启动samples,后可以访问：http://localhost:9494

端口可以改变，配置项：`com.alipay.sofa.lookout.prometheus-exporter-server-port`

## 3.可以配合 prometheus 服务查看

- prometheus.yml 编辑可以抓取该项目信息,假设本机IP：10.15.232.20

```
scrape_configs:
  - job_name:       'lookout-client'
    scrape_interval: 5s
    static_configs:
      - targets: ['10.15.232.20:9494']

```
- 本地运行 prometheus docker

```
 docker run -d -p 9090:9090 -v $PWD/prometheus.yml:/etc/prometheus/prometheus.yml  --name prom prom/prometheus:master
```

- 浏览器访问: http://localhost:9090