---
layout: doc
title:  encrypt
date:   2014-11-01 15:40:56
categories: doc
---

The encrypt that used with asyncrmi is TLS.

Once the encrypt is required in the [netmap file](netmap.html) for a client address the server will install the encrypt
filter after the handshake is done.

By default the server are using unique self signed key pair and certificate for each client connection.
If you wish to configure the server keys and certificate you can do that by setting the `sslServerContextFactory` in the
configuration object.

By default the client use trust store that accept any certificate, you can override it by setting the `sslClientContextFactory`
in the configuration object.

see [example](https://github.com/barakb/asyncrmi/blob/master/src/test/java/org/async/rmi/ServerTLSTest.java)
