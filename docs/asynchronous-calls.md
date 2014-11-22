---
layout: doc
title:  asynchronous calls
date:   2014-11-01 15:40:56
categories: introduction
---

## True asynchronous calls.
To achieve true asynchronous calls one have to implement it in the RMI protocol.
This is how it is done in asyncrmi.
First it is important that all the communications is done using Java NIO in non blocking mode,
thus sending a message is no more then putting the message on the selector queue and then the thread is free.

Each Request contains a unique request id sent to the server, the server copy this request id to its reply.
This make it possible to match the response to its request.
After the Request was put on the selector queue the proxy return Future to the caller this future is
saved in an internal data structure inside the proxy and when the reply comes from the server the proxy
resolve this future with the right value from the response.

<div class="diagram">
    Main->Proxy: invoke foo("bar");
    Proxy->Selector: Request(requestId=1\n,objectId=89, methodId=3\n,params=["bar"])
    Note right of Proxy: Proxy create a future\nf=Future<String>and\nsave it in with the context\nrequestId = 1
    Proxy->Main: return f
    Selector->Server: request
    Server->Selector: response
    Selector->Proxy: Response(requestId=1\n,result="value")
    Note right of Proxy: Proxy resolve f with "value"
</div>


