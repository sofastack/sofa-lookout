# 介绍 #
使用docker的方式来部署all-in-one-bootstrap.

# 构建镜像 #

在项目根目录执行
```bash
docker build -t lookout/allinone:1.0.0 .
```

> 为了在国内获得较好的构建速度, 构建时使用了aliyun的maven仓库.

## docker方式运行 ##
先运行ES
```
docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:5.6
```

服务端默认会连接到`localhost:9200`的ES实例, 而我所用的开发机器是MacOS, 无法使用`--net=host`模式启动容器, 因此在容器内无法通过`localhost:9200`连接ES, 需要使用如下方式绕过去:

编辑一个配置文件, 比如foo.properties
```properties
gateway.metrics.exporter.es.host=es
metrics-server.spring.data.jest.uri=http://es:9200
```

在foo.properties所在的目录下运行all-in-one镜像
```bash
docker run -it \
--name allinone \
--link es:es \
-v $PWD/foo.properties:/home/admin/deploy/foo.properties \
-e JAVA_OPTS="-Dlookoutall.config-file=/home/admin/deploy/foo.properties" \
lookout/allinone:1.0.0
```

> 这里利用了docker的--link参数使得应用可以访问到ES实例  
> 这里做测试用, 所以不用-d参数在后台运行


如果在linux上启动则quickstart例子无需上述设置, 直接执行如下语句即可:
```bash
docker run -it \
--name allinone \
lookout/allinone:1.0.0
```
