# SOFALookout

[![Build Status](https://travis-ci.org/alipay/sofa-lookout.svg?branch=master)](https://travis-ci.org/alipay/sofa-lookout)
[![Coverage Status](https://coveralls.io/repos/github/alipay/sofa-lookout/badge.svg?branch=master)](https://coveralls.io/github/alipay/sofa-lookout?branch=master)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![maven](https://img.shields.io/github/release/alipay/sofa-lookout.svg)

[English Document](./README_EN.md)

访问 [WIKI](http://www.sofastack.tech/sofa-lookout/docs/Home) 查看完整的文档使用指南。

SOFALookout 是一个利用多维度的 metrics 对目标系统进行度量和监控的项目。SOFALookout 的多维度 metrics 参考[Metrics2.0 标准](http://metrics20.org/)。SOFALookout 项目分为客户端部分与服务器端部分。

- 客户端是一个 Java 的类库，可以将它植入您的应用代码中采集 metrics 信息，[客户端更多详情](./client/README.md)。
- 服务端代码部分，对 metrics 数据进行收集、加工、存储和查询等处理，另外结合 [grafana](https://grafana.com)，可做数据可视化展示。

## 相较于常用的监控方案：

- Metrics 概念已经不算陌生，因此相比于其他没有数据标准的监控方案较为普适；
- 多维度的 metrics, 在传统的 metrics 的 name 基础上，又加上了一组 tags 的集合；因此相较于 Dropwizard, Spring Boot 的 actuator 等而言可以提供更丰富的 tags 维度方便监控分析；
- 相比于通过收集、加工应用系统产生的日志进行监控分析的方案，metrics 会显得更轻量，对系统资源的消耗也比较固定，不会随着业务量增加而增加；
- SOFALookout 除了对自身的 Java SDK 客户端采集源支持，还支持业界主流的采集 Agent 的数据汇报；
- 采集源可以通过配置文件或者服务发现机制找到 SOFALookout 的采集服务；
- SOFA 的体系中产品都会默认集成了 SOFALookout SDK 进行状态度量；
- 最后，当然监控领域也不存在银弹，基于 metrics 的 SOFALookout，适合用于宏观趋势的预警（比如一分钟内请求发生错误的次数），对定位某一次错误事件的问题并不擅长（比如，某一次调用的错误原因）。

我们在分布式场景已经积累了一定的经验，但也处于探索的过程中。开源 SOFALookout 项目，我们是希望以更开放方式来合作确保该项目向正确的方向持续演化。后续我们可能也会开源更多的解决海量数据的配套设施。

## 编译

- 客户端代码编译: `cd client && mvn clean install -DskipTests`
- 服务器端代码编译:
  - all-in-one模式: `./boot/all-ine-one-bootstrap/build.sh`, 打包结果在`boot/all-in-one-bootstrap/target/xxx-executable-ark.jar`
  - metrics-server: `mvn clean install -DskipTests -am -pl boot/metrics-server-bootstrap`
  - gateway: `mvn clean install -DskipTests -am -pl boot/gateway/bootstrap`
  
> 要求Maven 3.2.5+, JDK Version 8+；

## 样例工程
源码工程中提供了一些样例工程，辅助说明项目的使用。样例工程的 readme 有使用补充说明，另外需要将这些 sample 工程单独的导入 IDE。

### 客户端样例工程
- [lookout-client-samples-java](https://github.com/sofastack/sofa-lookout/tree/master/samples/metrics/client/lookout-client-samples-java)  
该样例工程展示了，在普通 Java 项目中,如何以代码形式使用和配置客户端。

- [lookout-client-samples-boot](https://github.com/sofastack/sofa-lookout/tree/master/samples/metrics/client/lookout-client-samples-boot)  
该样例工程展示了，在 SpringBoot(或SofaBoot) 项目中,如何使用和配置客户端。

- [lookout-client-samples-prometheus](https://github.com/sofastack/sofa-lookout/tree/master/samples/metrics/client/lookout-client-samples-prometheus)  
该样例工程展示了，在 SpringBoot(或SofaBoot) 项目中,如何使用和配置客户端使用 prometheus。

### 服务器端样例工程
coming soon...

## 贡献
如何参与 SOFALookout [代码贡献](./CONTRIBUTING.md)

## 致谢

Lookout 的 promQL 相关解析逻辑是从 Prometheus 移植而来，做了一些优化和改进， 感谢 Prometheus 开源了如此易用和强大的 golang 版本的 QL 实现

## 开源许可
SOFALookout 基于 [Apache License 2.0](./LICENSE) 协议，SOFALookout 依赖了一些三方组件，它们的开源协议参见[依赖组件版权说明](https://github.com/sofastack/sofa-lookout/wiki/NOTICE)。
