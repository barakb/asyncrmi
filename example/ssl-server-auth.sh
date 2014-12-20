#!/bin/bash
java -Djava.rmi.server.config=example/ssl.server.auth.config.yml -jar example/target/ssl-server.jar $*