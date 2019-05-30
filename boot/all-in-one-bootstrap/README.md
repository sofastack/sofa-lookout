# 运行方式 #
运行之前需要先启动ES实例
> docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:5.6

先打包
```
./boot/all-in-one-bootstrap/build.sh
```

在fat-jar同目录下创建一个`abc.properties`配置文件, 用于存放存放配置文件
```properties
gateway.metrics.exporter.es.host=localhost
gateway.metrics.exporter.es.port=9200
metrics-server.spring.data.jest.uri=http://localhost:9200
```

执行fat-jar包
```
java -Dcom.alipay.sofa.ark.master.biz=lookoutall \
-Dlookoutall.config-file=abc.properties \
-jar lookout-all-in-one-bootstrap-1.6.0-executable-ark.jar
```

> 注意 -Dcom.alipay.sofa.ark.master.biz=lookoutall 是必须的, 用于设置sofa-ark的master biz.  
> lookoutall.config-file 目前只能引用文件系统上的properties文件(没有像spring-boot支持那么丰富), 配置项必须以应用名开头, 从而提供隔离能力.
