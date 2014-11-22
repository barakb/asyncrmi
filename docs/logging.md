---
layout: doc
title:  logging
date:   2014-11-01 15:40:56
categories: doc
---

How to configure.
-----------------
Async RMI use SLF4J to manage log you will need choose a logging framework to use in case you are using log4j you can
add the following dependencies to you project:

```xml
 <dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.7</version>
</dependency>
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```
By default the logs of the communications between the client and the server is disabled.
You can enable it per interface class or method using the `@Trace`

For example:

```java
@Trace
public interface Server extends Remote {
    @Trace(TraceType.DETAILED)
    void addListener(EventListener listener);
    @Trace(TraceType.OFF)
    void removeListener(EventListener listener);
    void triggerEvent(EventObject event);
}
```

Will enable log for all communication to and from every instance of Server (unless override by subclass)
the logs are diabled for removeLister and will display the arguments as well as the call form addListener because its trace type is `TraceType.DETAILED`

see [@Trace](https://github.com/barakb/asyncrmi/blob/master/src/main/java/org/async/rmi/Trace.java)
and [@TraceType](https://github.com/barakb/asyncrmi/blob/master/src/main/java/org/async/rmi/TraceType.java)
- See messages and ip addresses.