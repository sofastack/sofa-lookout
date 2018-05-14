# Lookout Client Project

Lookout client is a java library which helps to expose metrics of your project. In contrast to traditional
hierarchical metrics, lookout client can provide multi-dimensional metrics with its API. It follows the
 [metrics2.0](http://metrics20.org/) standard.

# Features
- support for Java 6+
- [lookout-api](./lookout-api) can work alone, you can use it to collect metrics of your java project
- with [lookout-reg-prometheus](./lookout-reg-web) dependency, it will start a nested http server, and provides metrics query services in your project.
Prometheus can scrape metrics from this service.
- with [lookout-sofa-boot-starter](./lookout-sofa-boot-starter) dependency, this client can work on a spring boot project