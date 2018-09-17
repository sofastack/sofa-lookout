# SOFALookout Client

[English Document](./README-EN.md)

SOFALookout Client 是一个 Java 的开发工具库，可以为您的工程提供关键指标的度量服务。相较于传统的层级结构的 metrics，通过使用 SOFALookout client 的 API 可以为您提供多维度的 metrics。该客户端遵循 metrics 2.0 标准。

## 1. 编译

SOFALookout Client 项目支持 Maven 3.2.5+，JDK 6+ 进行编译。

## 2. API 埋点需知

lookout-api 支持被单独依赖和使用，方便植入您的项目代码，收集需要的 metrics，更多信息参考 [WIKI 文档](http://www.sofastack.tech/sofa-lookout/docs/Home)。

## 3. 扩展能力

lookout 客户端提供了 SPI 机制（只需要实现 `com.alipay.lookout.spi.MetricsImporter` 接口），支持可以扩展一些公共的Metrics收集模块，比如默认提供的：jvm（lookout-ext-jvm）、os（lookout-ext-os）。

## 4.Metrics的注册表

- lookout-reg-prometheus 模块可以提供简单的 metrics 的查询服务,作为 lookout 的 exporter 被 prometheus 抓取；
- lookout-reg-server 模块可以定时向 lookout server 上报 metrics 数据；
- lookout-reg-dropwizard 模块可以降维映射为 dropwizard 注册表上；
- lookout-api 模块自带 NoopRegistry 功能，如果当前运行环境未提供具体的 Registry 可用（比如，无客户端依赖）。

## 5.模块间依赖

```
             +--------+
             |   API  |
             +----^---+
                  |
             +----+---+
             | COMMON |
             +----^---+
                  |
+------+     +----+---+
| EXTS +----->  CORE  |
+--^---+     +----^---+
   |              |
   |         +----+---+
   |         |  REGS  |
   |         +----^---+
   |              |
   |         +----+---+
   +---------+ CLIENT |
             +--------+
```

## 6.如何使用

参考 [WIKI 文档](http://www.sofastack.tech/sofa-lookout/docs/Home)的快速开始和用户手册。
