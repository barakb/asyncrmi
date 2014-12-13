---
layout: doc
title:  encrypt
date:   2014-11-01 15:40:56
categories: doc
---
Here we will see how to generate a certificate authority keys ans sign with it the server and the client public keys.
A public key signed by certificate authority is called certificate and it is can be used to validate your identity
provide that you know the certificate authority public key is authentic.

We will use Java keytool that available on every Java platform.

```bash
keytool -genkeypair  -keysize 2048 -genkey -alias ca\
 -keyalg RSA -keystore ca.keystore\
 -storepass password -keypass password\
 -dname "CN=Certificate Authority, OU=Async, O=RMI, L=Avigdor, S=NA, C=ISRAEL"
```

This command will generate a private key and self signed public key (certificate) in the file `ca.keystore` with the alias `ca`

Next we create the `client` keys in its own keystore:

```bash
keytool -genkeypair  -keysize 2048 -genkey -alias client\
 -keyalg RSA -keystore client.keystore\
 -storepass password -keypass password\
 -dname "CN=client, OU=Async, O=RMI, L=Avigdor, S=NA, C=ISRAEL"
```

Now if you `list` the content of the client keystore.

```bash
keytool -list -v -storepass password -keypass password\
 -keystore client.keystore
```
you will see somethig like that:

```bash
Keystore type: JKS
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: client
Creation date: Dec 13, 2014
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=client, OU=Async, O=RMI, L=Avigdor, ST=NA, C=ISRAEL
Issuer: CN=client, OU=Async, O=RMI, L=Avigdor, ST=NA, C=ISRAEL
Serial number: c98c3c0
Valid from: Sat Dec 13 16:52:56 IST 2014 until: Fri Mar 13 16:52:56 IST 2015
Certificate fingerprints:
	 MD5:  B1:C7:22:B8:7D:5F:7C:08:8D:7D:99:11:79:7C:32:3A
...
```
What is important that the certificate chain length is 1 and the Owner and Issuer are the same, that is because the certificate is self signed.

In order to establish a certificate chain from the client public key to the ca we need to export the
`ca` certificate into the `client` keystore, we export the `ca` certificate as trusted certificate since it is our root certificate authority.
Here is the command for that:

```bash
keytool -keystore ca.keystore -alias ca\
 -storepass password -keypass password -exportcert | \
keytool -keystore client.keystore -alias ca-certificate\
 -storepass password -keypass password\
 -v -noprompt -trustcacerts -importcert
```

your cmd should output the following:

```bash
Certificate was added to keystore
[Storing client.keystore]
```
That command export the ca certificate and pipe it to import to the client keystore as trust certificate `-trustcacerts`

If you now list the client keystore file you will see 2 aliases in there

```bash
keytool -list -v -storepass password -keypass password\
 -keystore client.keystore
```

```bash
Your keystore contains 2 entries
...
Alias name: client
...
Alias name: ca-certificate
```

but still the certificate chain length is 1, the certificate and the client public key are not connected.
In order to connect them you have to send certificate request to the certificate authority and import
the replied certificate to the client keystore with the *client alias*

This is done with the following cmd:

```bash
keytool -keystore client.keystore -alias client\
 -storepass password -keypass password -certreq | \
keytool -keystore ca.keystore -alias ca\
 -storepass password -keypass password -gencert | \
keytool -keystore client.keystore -alias client\
 -storepass password -keypass password\
 -noprompt -importcert
```

This should result with the output:

```bash
Certificate reply was installed in keystore
```

Now if you list the client keystore file:

```bash
keytool -list -v -storepass password -keypass password\
 -keystore client.keystore
```
You should see that the Certificate chain length is 2 and the issuer is `Issuer: CN=Certificate Authority`

```bash
....
Alias name: client
Creation date: Dec 13, 2014
Entry type: PrivateKeyEntry
Certificate chain length: 2
Certificate[1]:
Owner: CN=client, OU=Async, O=RMI, L=Avigdor, ST=NA, C=ISRAEL
Issuer: CN=Certificate Autority, OU=Async, O=RMI, L=Avigdor, ST=NA, C=ISRAEL
....
```

[Here](https://github.com/barakb/asyncrmi/blob/master/example/src/main/scripts/keysgen.sh)
is script that create the client and server keystores signed by the certificate authority

At this stage you have a Java keystore that contains the client private key and certificate signed by ca.
You can use Java code to import each from the jks file to a pem file.
Basically a pem file is jus a text file that has the key or certificate encoded in utf8 (protected by simple password optionally) with some delimiters lines before and after.

For example:

```bash
(master) barakbo-pcu:keys $ cat client-private.pem
-----BEGIN PRIVATE KEY-----
MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCFFFKi7i1jRube7tQbmZL2sV2T
7i5ypUnwjK7QHRL6l0nEnmmAAaJ1rbMf8s3XC6cVuo0UtQUATDPUaU2sg01vQnNXe/B1ycFMlVM5
pRAPhIBfi2zv3fh+VyxTV/tN2k2qnGpwp/qnVpQdf0DnlI/U5suMOo+/rHBuAFPGsPv5ZeBdG7xo
+NmD5DnFJSa19gxDzqcR6tsi3by5bPXKPxi3n0ADVWis2FDSlsKsxlksn0ne12QXpL3lygtDaagJ
L+CmJt2D0+BSjJOzc620p+JCFrElcTk/IbARGRSHNj1IRZWxL844NtF/TVKqZOjJkuN/Eg6kktjm
XcuHggA9w2nBAgMBAAECggEAIL/fljkG8meAaVxc1m30vMDpRn5W79+9UnVMj+qXwRN6E+joDZhZ
YpxceIBIJ6O2eqJuFap2kJwwJRXFB69IXxj7SYw2oMYFy9LzqDv2t9rjvJ+TL3RwsqbBQcFlFsOi
WVok15R/Gq8zxA6M6nCk9L/XV6sRtRm6kVeDRmJHoBgWObkVcp9/fMyG7cIdQHd+3dg3unHvcFsd
N/FmxtiY7FQNfPVOz6SxREAds+LM3JtD2SF1n6NFcYtlLvQ/jhqHOJ+BB2yEOjp7LayjUCPRozKS
vjaP9hLcDVCG7aUfpdg+7hWpN5PSDpdBm+WR3TD3DYke85i3N505KPsM1UGsAQKBgQDW1hRnEMrr
o5Nlyb0N/M3fuPxApqf/q9q6fd206doLfMu2GWpd856FKtmMrnUZNCDEIn+o6kc3DzCGA6ULok8o
+q5PDqLSFtwbm0AEDeFivylnWQSbmKzVUYNPXAGQ9JlcUtuSpBkAWphurJQlvOKYGl2RpLc6SRs8
BL/LLfqVmwKBgQCek/3zIHtbJDBCzYx+RQShB2hoJ41d99wwqQqzyxJC/i0IoLsb6i+fUqNGcBZR
P/v5urwRgk2zNM8AQml1wuiBhkjpRLj/ZVxR3q5QTkf+Ylold/WIn876vuCfnEjHlNE6SKs6WkI8
R5s8k4r0yJSLaswpv/16zK58HFezyXWB0wKBgQC4CDL6BvyM1lbRfvsgHEsPNpt/lkObVE/OKTve
WeSaqpCmsSbYzHQihw5M4LUWnfzjhtnsjKPsmmpqonyjdqpw5CylNFhtt39ddqRj1LAZsr949Fxq
HWMQaP8e4CXHUoB9Ac1t1HxGPVUOmW9e1GsW+J7mwD7lAvL9euQA3KYnuQKBgQCadvNNyJz/2U89
MvUf5eCtbgbQNX6pQo7zwyUOnsbqBKYfGllHxqSen6M1MGc8u4OY7MDWmUNQRSdBTG9navSJfiE1
P2CoPbZgdPT8elP8QRcoGiJL88ZsTlOEhXoLFjV7fV90loigKOvYZKtc5qhy6RN3s3CYFhn8AKuZ
D6prSwKBgQCH/ctktUIfm65S6IAq1AzD16aLkHABbTWiLGEzSXnJWuIQ/kIQzP3U2X3hbkHoNu+F
AvsUyFlrcLTP13+xeqZI9cWu2POAa71SYjqoh9V4LKuK2d4Gq035zGKWs4/YIeCKuL4kPUgGe7Oo
cbMBqTctIZKIptB4RTkvQiYpUiNEkA==
-----END PRIVATE KEY-----
```

[Here](https://github.com/barakb/asyncrmi/blob/master/example/src/main/java/org/async/example/ssl/ExportJKSToPem.java)
is some JavaCode that export key and certificate from JKS file to pem files

A full example with the script that generate the keys is available [here](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/ssl)