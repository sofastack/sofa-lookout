# Lookout  API User Guide

## 1.Import API dependency into your project

This is a minimum dependence.

```xml
<dependency>
	<groupId>com.alipay.lookout</groupId>
	<artifactId>lookout-api</artifactId>
	<version>${lookout.version}</version>
</dependency>
```

You can get the global registry instance from the method "Lookout.registry()".
By the way the default registry is NoopRegistry, if no other registry implementation is provided.

## 2.How to create a metric Id

 Lookout metric Id is constructed with name and tags.

 ```java
 Id id = registry.createId("rpc.provider.service.stats");
 basicId = id.withTag("service", "com.alipay.demo.demoService")
             .withTag("method", "sayHi")
             .withTag("protocol", "tr")
             .withTag("alias", "group1");
 ```
 The demo codes show how to create a new Id and attach tags to it.
 A new Id instance will be created, when the `withTag` method is invoked.

#### 2.1 Priority Tag (Non-mandatory)

Priority Level: HIGH, NORMAL, LOW;

```java
id.withTag(LookoutConstants.LOW_PRIORITY_TAG);
```
The default priority level is normal, so you don't have to put a priority tag.The priority level represents a time interval of data collecting.
A metric with a higher level will be collected more timely.

#### 2.2 Tags

 - Common tags, such as: machine ip, cluster name, datacenter name ...
 - you can customize common tags with the interface `com.alipay.lookout.client.LookoutClient`. Don't forget application name tag(`tag:app=xx`)
 - Common tags can be attached to all metrics uniformly
 - The name(key) fo a tag must be lowercase. Letters, numbers, underscores are recommended
 - The number of tags is as small as possible
 - The value of the tag should be enumerable


## 3. Metric Types


- Counter

```java
Counter counter=registry.counter(id);
counter.inc();
```

- Timer

```java
Timer timer=registry.timer(id);
timer.record(2, TimeUnit.SECONDS);
```

- DistributionSummary

```java
DistributionSummary distributionSummary=registry.distributionSummary(id);
distributionSummary.record(1024);
```

- Gauge

```java
registry.gauge(id,new Gauge<Double>() {
    @Override
    public Double value() {
        return 0.1;
    }
});
```

- MixinMetric


```java
//1. getOrAdd  MixinMetric
MixinMetric rpcServiceMetric=registry.minxinMetric(id);

//2. getOrAdd basic component metric to use
Timer rpcTimer = rpcServiceMetric.timer("perf");
DistributionSummary rpcOutSizeMetric = rpcServiceMetric.distributionSummary("inputSize");
```

- Top tool

Only record top(5, 10, 20 ...) metrics in memory.

```java
TopGauger topGauger = TopUtil.topGauger(registry, registry.createId("top5sql"), 5);

topGauger.record(1000l, new BasicTag("sql1", "select1"));
topGauger.record(2000l, new BasicTag("sql2", "select2"));
...
```

- Info()

Non-numerical data.

TO BE CONTINUE...







