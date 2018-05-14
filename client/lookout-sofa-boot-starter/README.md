## 1. How to get the instance of `com.alipay.lookout.api.Registry`?

- inject the instance by this type or by its name `registry`


## 2. Integrate with spring boot actuator

- metrics collected by Lookout-api can be mapped to Actuator

The dropwizard dependency is required as a bridge.

```xml
<dependency>
	<groupId>io.dropwizard.metrics</groupId>
	<artifactId>metrics-core</artifactId>
</dependency>
```
