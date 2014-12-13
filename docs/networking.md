---
layout: doc
title:  networking
date:   2014-11-01 15:40:56
categories: introduction
---

## Network configuration

Currently network confguration contains the following properties that can be set either from the code or from a yml file.

```java
    private int configurePort = 0;
    private TimeSpan clientConnectTimeout = new TimeSpan(30, TimeUnit.SECONDS);
    private TimeSpan clientTimeout = new TimeSpan(30, TimeUnit.SECONDS);
    private String serverHostName;
```
- `configurePort` set the port for this process server if null the port will be random, the value of this
random port will be available thru the configuration property `actualPort`
- `clientConnectTimeout` is the maximum timeout for a connect from a client.
- `clientTimeout` is a maximum timeout that the server is willing to wait for a response from the client.
- `serverHostName` is used to set the name of the host/ip in case of multiple network interfaces.

setting those values using yml file is simple:
create a yml file with the content:

```yaml
---
configurePort: 1

clientConnectTimeout:
    time: 1
    unit: minutes

clientTimeout:
    time: 30
    unit: seconds

serverHostName: myHost
```

in addition it is possible to map filters per client connection,
at this time the only filters are `compress`, `encrypt` and `drop` that is not a real filter.

* `compress` is used to compress the network traffic between the server and a specific client.
* `encrypt` use to encrypt the communication to and from the client using TLS.
* `drop` use to let the server know that it is not allow to let some client to connect, the connection will be closed.

When client is try to connect to a server the sever check the client host name and host ip against the match section of each network mapping rules.
This is very similar to what is done in firewalls.
The match part is a regular expression that if match to the client ip or name determain the set of filters for this connection.
Once set the server let the client know what filters are required and the handshake is done.
Here are some examples:

```yaml
---
configurePort: 1

clientConnectTimeout:
    time: 1
    unit: minutes

serverHostName: myhost

netMap:
    rules:
        - match: 82.*
          filters: []
        - match: 8.*
          filters: [compress]
...
```
With this configuration every client from ip starts with 8 but not have 2 as it second number will use compression.


```yaml
netMap:
    rules:
        - match: 8.*
          filters: [encrypt, compress]
        - match: .*
          filters: [drop]
...
```

With this configuration every client from ip starts with 8 will be encrypt and compress, all other connections will be closed.

The compression that is done that way is done with new keys that the server and the client compute on the fly for this session.
This is not very optimal but if you just need to encrypt the line it is very easy with almost zero configuration.

If however you wish to provide your own keys please read the [tls docs](tls.html)

- benefits.
- satellite line.
