#!/bin/bash

LOG4J_CLASSPATH=example/deps/slf4j-log4j12.jar:example/deps/log4j.jar
ASYNC_RMI_CLASSPATH=target/asyncrmi-1.0.3-dep.jar

java -cp ${LOG4J_CLASSPATH}:${ASYNC_RMI_CLASSPATH}:example/target/file-client.jar org.async.example.resultset.client.ClientImpl $*


