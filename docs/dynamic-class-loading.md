---
layout: doc
title:  dynamic class loading
date:   2014-11-01 15:40:56
categories: doc
---

When RMI send object between JVMs only the object data is transferred it is assume that the class definition
already present on the other JVM when this is not correct the RMI on that other machine have to some how find
and load the missing class definition.

RMI use the concept of codebase to find the place where a class should be loaded from.
Each JVM can be configure with codebase that is sequence of URLS that sent with each object by the RMI.
For example if I run my program with `java -Djava.rmi.server.codebase="url1 url2 url3" -jar myclient.jar`
The server that this client will connect to will try to get the missing classes from url1 url2 and url3
this is because the client will sent with each object the object codebase that is in our case "url1 url2 url3"
If however the client got some object from another RMI server this object will have the original codebase with it.
I will explain it with a sequence diagram.

Lets assume that server1 is run with `-Djava.rmi.server.codebase="http://server1/code"`
Server2 run with `-Djava.rmi.server.codebase="http://server2/code"`
And the client run with no codebase configure.

<div class="diagram">
    client->server1: getObject()
    server1->client: object
    client->server1/code: GET object.class
    server1/code->client: object.class
    client->server2: setObject(object)
    server2->server1/code: GET object.class
    server1/code->server2: object.class
</div>

With classic RMI the configuration of codebase was not easy while developing.
Async RMI try to be more user friendly during the development phase.
If you do not set the property `java.rmi.server.codebase` it will be set for you and an HTTP server will started
at random port to serve you file.
If you wish to run this internal http server at a fix port all you have to do is set the `java.rmi.server.codebase` to
this port number, for example:

`java -Djava.rmi.server.codebase="3030" -jar myclient.jar`

If you have your http server that serve as codebase server you can use the regular RMI syntax:

`java -Djava.rmi.server.codebase="url1 url2 url3" -jar myclient.jar`


[Here](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/dcl) you can find a working example of dynamic class loading.

