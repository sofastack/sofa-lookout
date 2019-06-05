#!/usr/bin/env bash

# all in one 打包方式
mvn clean install -T 1C -Psofaark -Dmaven.test.skip=true -am -pl boot/all-in-one-bootstrap


# 单独打包方式, 需要排除all-in-one模块, 否则会报错
