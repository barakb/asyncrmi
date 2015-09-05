#!/bin/bash

mvn clean install &&  (cd example; mvn clean install)
