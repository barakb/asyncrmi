---
layout: doc
title:  oneway calls
date:   2014-11-01 15:40:56
categories: introduction
---

#Why do we need one way calls?

When client invoke a remote method this is the sequence of events:

<div class="diagram">
    client->clientsocket:request
    clientsocket->serversocket:request
    serversocket->server:request
    server->serversocket:response
    serversocket->clientsocket:response
    clientsocket->client:response
</div>

Some time when the response return null and the request acknowledgment is not so important
it is desirable to save the network by not sending a response from the server at all.

This is done using the `@OneWay` asyncrmi annotation.

when using the `@OneWay` annotation on a method
(here is an [example](https://github.com/barakb/asyncrmi/blob/master/example/src/main/java/org/async/example/dcl/EventListener.java))
The sequence diagram should look like that:

<div class="diagram">
    client->clientsocket:request
    clientsocket->serversocket:request
    serversocket->server:request
</div>

It is possible to define one way method that returns a `Future<Void>` this means that the
future will be resolved as soon as the clientsocket got the request if the oneway annotation is full `@OneWay(full = true)`
or after the request was fully sent from the clientsocket if the oneway annotation is not full `@OneWay(full = false)`

Note that full oneway future will never throws anything while not full oneway future can throw an exception in case the message was not be able to sent to the server.
Here are both sequence diagrams:

## `@OneWay(full = true)`
<div class="diagram">
    client->clientsocket:request
    Note right of clientsocket: client future resolved\nbefore message\nsent to server socket.
    clientsocket->serversocket:request
    serversocket->server:request
</div>

## `@OneWay(full = false)`
<div class="diagram">
    Title: @OneWay(full = false)
    client->clientsocket:request
    clientsocket->serversocket:request
    Note right of clientsocket: client future resolved\nafter message\nsent to server socket.
    serversocket->server:request
</div>

The default one way mode for asyncrmi is `@OneWay(full = true)`
