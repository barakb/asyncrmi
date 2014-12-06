---
layout: doc
title:  handshake
date:   2014-11-01 15:40:56
categories: introduction
---

## Protocol identifier.
Since the Async RMI messages are framed in frame that has the message message length as the first 4 bytes of the message
it is possible for another client to send a message to an RMI server that does not contains real message and have
a very large number in the first 4 bytes of the message accidentally or maliciously.
This will cause the server to allocate huge buffer and may result in Memory error.
It is also possible for a proxy to connect to a server that is no more running but another process listen to server port.
In that case it is possible that the process will reply to the proxy with illegal message and as a result the proxy will
run out of memory.

To prevent that the protocol handshake identifier is performed when connection is established.
This protocol use fix length messages to prevent out of memory.

<div class="diagram">
    client->server: asyncrmi[challenge byte][4 zero bytes]
    server->client: asyncrmi[challenge + 1][network filters 4 bytes]
</div>

Messages are 13 bytes length.

The server adjust its network filters before sending the reply to the client.
The client adjust its network filters before sending the first none handshake message.

When the server send its handshake message to the filter it contains the set of network filters that the server will be use
for this session, this set can be configure per client address in the netmap.yaml file.
By default the name of the file is netmap.yaml and if such file exists it is used.
You can point asyncrmi to another file with the system property `java.rmi.server.netmapfile`

The syntax of the network mapper file is describe [here](netmap.html)

You can set the content of this configuration from inside Java program using the netmap variable inside the configuration object.





