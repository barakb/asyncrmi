SSL Example
---------------------
- Server configuration in the file ssl.server.config.yml
- Client configuration in the file ssl.client.config.yml

## Compile
From the asyncrmi folder type `./build.sh`

## Build the keys and certificates.
The keys can be build by running the bash script `./keysgen.sh` in the folder `example/src/main/scripts`
What it does is building 2 jks files in the folder `example/src/main/keys`

- `ca.keystore` is a keystore with the certificate authority keys.
- `client.keystore` is a keystore with the client keys certificate by the certificate authority.
- `server.keystore` is a keystore with the server keys certificate by the certificate authority.
## Run

*from the asyncrmi root folder* type
`./example/ssl-client.sh true` and client `./example/ssl-client.sh `
The true in the server cmd is a request to export the jks keys and certificate to pem format.

## Configuration
The server configuration is in the file `ssl.server.config.yml`
and the client configuration is in the file `ssl.client.config.yml`

Minimal server configuration that enable SSL is:

```yml
---
netMap:
    rules:
        - match: .*
          filters: [encrypt]
...
```

The client does not need any configuration since it get the request to load SSL at handshake time
and generate self signed keys on the fly when not there is no SSL configuration.

## Client auth.
It is possible to configure the server to require the client to authenticate.
This is done with the `auth: path` section in the rule.
The path is a path to a pem certificates file, only clients with public key certify by one of the given certificate (maybe by a chain) is allow to connect.
see `ssl.server.auth.config.yml` and `ssl-server-auth.sh`

```yml
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
