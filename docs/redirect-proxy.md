---
layout: doc
title:  redirect clients
date:   2014-11-01 15:40:56
categories: introduction
---

Some time it is desired to close the proxy, maybe to stop another thread from using it or some other resource.
The correct way to do that is to cas the proxy to `org.async.rmi.Exported` interface and to call close:

With Async RMI it is possible to redirect the proxy server other then the serve that it was exported from provided that the new
server implement the same interface as the original one.
This is useful for implementing consensus protocol like raft, paxos and zab.
In which one does not have a place to get a proxy so each server can create its own proxy and redirect it to one of the other servers.

The redirection is done from the `Exported` interface by provided a new host port and objectId.
Sine the object id is as rule created automatically when exporting an object, it is also possible to export an object at
explicit objectId provided that the objectId is negative number, sine all non negative numbers are reserved for system use.

```java
Constant<String> fixedServer = new ConstantServer<>("fixed");
Modules.getInstance().getExporter().export(fixedServer, -1);
```

After that you can take any client of type `Constant` and redirect it to this server.
For example:


```java
((Exported) client).redirect(-1, "localhost", configuration.getActualPort());
```
after that client will be connected to fixedServer instead of it originator server.

See [example](https://github.com/barakb/asyncrmi/blob/master/src/test/java/org/async/rmi/CloseTest.java).