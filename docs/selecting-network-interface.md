---
layout: doc
title:  selecting network interface
date:   2014-11-01 15:40:56
categories: introduction
---

Some time it is desired to publish the server on specific network, for example the server is running on multi network machine and has to
publish it services only to one network.
It can be done with the standard RMI java.rmi.server.hostname system property.
Where the value can be IP or the name of the server in that network.

