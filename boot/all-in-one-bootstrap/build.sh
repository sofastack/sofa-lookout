#!/usr/bin/env bash

current_dir=$( dirname $0)
cd $current_dir/../..
mvn clean install -T 1C -Psofaark -P!single-bootstrap -Dmaven.test.skip=true -am -pl boot/all-in-one-bootstrap
