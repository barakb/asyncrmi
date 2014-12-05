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
    client->server: asyncrmi[4 byte challenge][byte 0][sh-256 of all the prev]
    server->client: asyncrmi[challenge + 1][control byte][sh-256 of all the prev]
</div>
If the control byte is zero the client switch to length field based frame protocol and send the first message to the server.

If the control byte is not zero the client have to send ack in fix size protocol, it send the last server message as ack.

<div class="diagram">
    client->server: asyncrmi[challenge + 1][control byte][sh-256 of all the prev]
</div>

After that the client and the server change protocol to length field based frame instead of fix size message
and the first message is the network filters from the server


That way it is possible for the client and the server to know that the other side is following the same protocol.





