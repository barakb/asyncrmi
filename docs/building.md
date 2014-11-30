---
layout: doc
title:  building
date:   2014-11-01 15:40:56
categories: doc
---

Pre requisite
-------------
* Oracle JDK8.
* Maven 3.


Downloading
-----------

You can download the sources as [zip](https://github.com/barakb/asyncrmi/archive/master.zip)
, [tar.gz](https://github.com/barakb/asyncrmi/archive/master.tar.gz) or you can clone the git repository.

`mvn install` will build the project.


Building the examples
----------------------

from the example sub dir type `mvn install`


Alternativly you can get (or build) a Docker image with the dev env:

1. sudo docker pull barakb/asyncrmi
2. sudo docker run -i -t --name=asyncrmi  barakb/asyncrmi /bin/bash
3. git pull --rebase
4. mvn install


