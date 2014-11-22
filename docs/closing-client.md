---
layout: doc
title:  closing client
date:   2014-11-01 15:40:56
categories: introduction
---

Some time it is desired to close the proxy, maybe to stop another thread from using it or some other resource.
The correct way to do that is to cas the proxy to `org.async.rmi.Exported` interface and to call close:

```java
((Exported)client).close();
```

After that any invocation of this client will throw `org.async.rmi.client.ClosedException` that is a RuntimeException.
The `ClosedException` will be thrown even when the invoke method is annotated with `@OneWay(full = true)`.

See [example](https://github.com/barakb/asyncrmi/blob/master/src/test/java/org/async/rmi/CloseTest.java).