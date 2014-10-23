[![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=master)](https://travis-ci.org/barakb/asyncrmi)
asyncrmi
========

#Goals

A Modern Java Asynchronous RMI Implementation

- Support asynchronous calls.
    * Futures and futures listeners.
- Support multiple threads policies.
    * NIO.
- Easy To use.
    * Easy class loading.
    * No code generation.
    * Use Oracle RMI marker interfaces when possible.
    * Closures on top of futures and streams.
- Easy to read, understand and debug.
     * Use 3rd parties such as:
        + [netty](http://netty.io/)
        + [slf4j](http://www.slf4j.org/)
- Production ready.
- High performance.
