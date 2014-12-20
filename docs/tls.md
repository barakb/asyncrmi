---
layout: doc
title:  Generating keys.
date:   2014-11-01 15:40:56
categories: doc
---

The encrypt that used with asyncrmi is TLS.

By default if required the server and client are using unique self signed key pair and certificate for each client connection.

It is only required to configure the server to require encryption from each client to have all communication but the handshake encrypted.
This is the server configuration file needed for this:

```yaml
---
netMap:
    rules:
        - match: .*
          filters: [encrypt]

...
```
If you wish to use your one keys for the process you have to add id section in the server config file.
In the id section you have to provide the path to pem private key file, and the
path to a pem certificate file (that is the server public key signed by a certificate authority).
you can learn how to create your set of keys and certificate [here](keys.html)


```yaml
---

netMap:
    rules:
        - match: .*
          filters: [encrypt]

    id:
        key : example/src/main/keys/server-private.pem
        certificate: example/src/main/keys/server-certificate.pem
...
```

With this configuration the server/client will use the provided key and certificate instead of generating one on the fly.
This is useful if you wish to authenticate the client using certificate.

To authenticate the client the rule have to include one more element the `auth` element that point to a file that
contains the pem certificate that should be used to authenticate the client.

 ```yaml
 ---
 netMap:
     rules:
         - match: .*
           filters: [encrypt]
           auth: example/src/main/keys/client-certificate.pem

     id:
         key : example/src/main/keys/server-private.pem
         certificate: example/src/main/keys/server-certificate.pem
 ...
 ```

see [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/ssl)
