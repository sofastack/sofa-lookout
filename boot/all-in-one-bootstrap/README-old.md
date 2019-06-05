# 打包方式 #
```
./boot/all-in-one-bootstrap/build.sh
```

# 运行方式 #
运行之前需要先启动ES实例
> docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:5.6

> 注意, 根据目前的项目结构, all-in-one-bootstrap 必须先打包成jar, 然后才能运行, 不能直接运行main方法.
> 原因是 gateway-bootstrap, metrics-server-bootstrap与all-in-one-bootstrap在同一个工程里, 因此IDEA会直接引用这2个模块的源代码, 而sofa-ark会对打包结果做一些修改, 因此导致无法正常工作.  

```
java -Dcom.alipay.sofa.ark.master.biz=lookoutall \
-Dlookoutgateway.config-additional-location=config-dir-for-lookoutgateway \
-Dlookkoutserver.config-additional-location=config-dir-for-lookoutserver \
-Dlookoutall.config-file=abc.properties \
-Dlookoutgateway.foo=bar \
-Dlookoutserver.bar=baz \
-jar allinone-executable.jar
```

> 注意 -Dcom.alipay.sofa.ark.master.biz=lookoutall 是必须的, 用于设置sofa-ark的master biz.  
> lookoutall.config-file 目前只能引用文件系统上的properties文件(没有像spring-boot支持那么丰富), 配置项必须以应用名开头, 从而提供隔离能力.


## 可选启动 ##
可以用以下2个系统属性或环境变量来控制允许启动的app黑白名单.
```
sofaark.embed.apps.whitelist=app1,app2
sofaark.embed.apps.blacklist=app3,app4
```

> 目前实现的方式比较简单


# 配置管理 #
对于一个普通的`Spring Boot`应用来说, 它的常用的配置来源有以下几种:
- classpath下的配置文件
- 环境变量
- 系统属性

当使用`sofaark`的静态合并部署功能时, 上述几种配置来源都可以正常工作, 只是需要考虑配置隔离的问题: 假设两个应用都根据`server.port`参数来决定自己暴露的HTTP端口号, 我们配置`-Dserver.port=8080`, 那此时两个应用都会暴露在8080端口, 出现冲突.

我们采取的策略是:
1. classpath下的配置文件利用sofa-ark的隔离能力, 本身就可以做到隔离
2. (可选)在`all-in-one-bootstrap`项目的`resources/app-configs`目录下还可以提供针对各个应用的更高优先级的配置, 实现方式是会在运行时, 将这些目录复制到临时目录, 然后利用SpringBoot的`additional-config-location`使得对应目录的配置文件生效.
3. 环境变量和系统属性采用 `Spring的Environment抽象 + 前缀法` 获取隔离能力
	1. 这部分参数通常是部署时才确定的参数

因此运行时, 一个`sofa-ark-biz`的应用(刚好都是SpringBoot技术栈)的配置来源优先级从高到低是:
1. [新] 前缀系统属性
2. [新] 前缀环境变量
3. 系统属性
4. 环境变量
5. [新] app-configs/<appName> 下的配置文件
6. 原classpath下的配置文件

> 标记为 [新] 的是新引入的配置, 其余的都是SpringBoot内置的

## 传入配置 ##
根据上文, 传入配置的方式有以下几种, 以lookoutgateway为例, 优先级从高到低:
1. 运行时设置系统属性 `-Dlookoutgateway.foo=bar`, 只对lookoutgateway应用生效, 相当于配置了 `foo=bar`
2. 运行时设置环境变量 `lookoutgateway.foo=bar`, 只对lookoutgateway应用生效, 相当于配置了 `foo=bar`
3. 通过设置 -Dlookoutall.config-file=文件系统上的一个properties文件, 该文件中, 所有已 `lookoutgateway.`开头的配置项会对lookoutgateway应用生效
4. 原有的系统属性
5. 原有的环境变量
6. 通过 lookoutgateway.config-additional-location 指定的只针对lookoutgateway生效的配置文件目录, 这个目录将会有spring-boot来解析, 所以spring-boot的特性在这里是可以利用的
7. lookoutgateway的classpath的application配置文件

假设 `abc.properties` 文件有如下内容
```properties
lookoutgateway.server.port=7300
lookoutserver.server.port=7100
```

以如下命令启动时
```
java -Dcom.alipay.sofa.ark.master.biz=lookoutall \
-Dlookoutall.config-file=abc.properties \
-jar allinone-executable.jar
```

`server.port=7300`会作用于`lookoutgateway`子应用, `server.port=7100`会作用于`lookoutserver`子应用.
