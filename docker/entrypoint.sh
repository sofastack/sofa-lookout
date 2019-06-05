#!/usr/bin/env bash

# all in one entry point

java_bin=java
app_jar=/home/admin/deploy/app.jar

# 构建 JVM 系统属性
java_opts="-Dcom.alipay.sofa.ark.master.biz=lookoutall"
java_opts="$java_opts $JAVA_OPTS"

echo $java_bin -server $java_opts $app_jar
$java_bin -server $java_opts -jar $app_jar
