---
layout: doc
title:  network mapping file
date:   2014-11-01 15:40:56
categories: introduction
---

The server can choose what set of network filter should be used with each client base on the client ip.
For now the only supported filters are [encrypt](tls.html) and  `compress` but you can also use `drop` which is not a real filter
to not allow any connection from some addresses.

The configuration is done via yaml file or from the java code via the netmap element inside the configuration object.
The yaml syntax is:

```yaml
---
rules:
    - match: .*
      filters: [encrypt, compress]
    - match: 192\.168\.2\.106
      filters: [drop]
    - match: 192\.168\.3\.106
      filters: [compress]
...
```

Each rule has match part that contains regular expression and filters part that has list of filters.
For each client try to connect the list of the rules are scan from the top until one of the regular expression match the
host name or the host address.
At that point the search stop and the list of filters returns.

If no rule match the empty list is returned.

By default the name of the file is netmap.yaml and if such file exists it is used.
You can point asyncrmi to another file with the system property `java.rmi.server.netmapfile`

