---
layout: default
title:  async rmi docs
date:   2014-11-01 15:40:56
categories: doc
---

##Introduction

1. [why did i started this project](why-did-i-started-this-project.html).
2. [rmi pros](rmi-pros.html).


## Async RMI Feature set.
<ul class="features">

    <li>Asynchronous calls [<a href="asynchronous-calls.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/futures">example</a>].</li>
    <ul class="features">
        <li>Java 8 <a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html">CompletableFuture</a>.</li>
        <li>Client timeout [<a href="client-timeout.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/timeout">example</a>].</li>
        <li>One way calls [<a href="oneway-calls.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/oneway">example</a>].</li>
    </ul>

    <li class="notready">Configurable thread policies [<a href="threads.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/oneway">example</a>].</li>

    <li>Dynamic class loading [<a href="dynamic-class-loading.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/dcl">example</a>].</li>

    <li>Ease of use.</li>
    <ul class="features">
        <li>Maven build [<a href="building.html">documentation</a>].</li>
        <li>Use Oracle RMI marker interfaces, Remote, RemoteException.</li>
        <li>Logging [<a href="logging.html">documentation</a>].</li>
        <li>Mo code generation.</li>
        <li>Support Closures and Streams.</li>
        <li class="notready">Work behind firewalls</li>
        <li>Automatic exporting remote objects [<a href="automatic-exporting.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/dcl">example</a>].</li>
    </ul>

    <li class="notready">Distributed Garbage collection</li>
    <li>Client connection pool</li>
    <li>Requests pipeline [<a href="request-pipeline.html">documentation</a>] [<a href="https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/pipeline">example</a>].</li>

    <li>Networking</li>
    <ul class="features">
        <li>Unpublish [<a href="unpublish.html">documentation</a>].</li>
        <li>Selecting network interface [<a href="selecting-network-interface.html">documentation</a>].</li>
        <li>Handshake [<a href="handshake.html">documentation</a>].</li>
        <li>TSL [<a href="tls.html">documentation</a>].</li>
        <li >Closing proxy [<a href="closing-client.html">closing-client</a>] [<a href="https://github.com/barakb/asyncrmi/blob/master/src/test/java/org/async/rmi/CloseTest.java">example</a>].</li>
    </ul>

</ul>
