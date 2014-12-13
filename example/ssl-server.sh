#!/bin/bash
java -Djava.rmi.server.config=ssl.server.config.yml -jar example/target/ssl-server.jar $*