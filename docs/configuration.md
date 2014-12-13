---
layout: doc
title:  configuration
date:   2014-11-01 15:40:56
categories: doc
---

Configuration can be done from the code or with config file.
For code configuration refers to the class
[org.async.rmi.config.Configuration](https://github.com/barakb/asyncrmi/blob/master/src/main/java/org/async/rmi/config/Configuration.java)
The instance of the current configuration can be access using the following code `Modules.getInstance().getConfiguration()`
After that you can modify the configuration object, notice however that for some of the values it is not make sense
to be changed after an export was called.

The configuration can injected to a program from file.
By default the file `config.yml` if exists is used but this can be override using Java system properties.
For example `-Djava.rmi.server.config=some other file`
The format of the configuration file is [yaml](http://www.yaml.org/)
this format is very readable but it has grammar that is not context free, indentation is important.

Here is an example of yml configuration.

```yaml
---
configurePort: 1

clientConnectTimeout:
    time: 1
    unit: minutes

serverHostName: myhost

netMap:
    rules:
        - match: .*
          filters: [encrypt]

    id:
        key : example/src/main/keys/server-private.pem
        certificate: example/src/main/keys/server-certificate.pem
...
```

You can easily match between the `org.async.rmi.config.Configuration::setConfigurePort()` to the `configurePort` in the config file.
When some of the configuration is missing from the file the defaults are taking in.

In the network mapping configuration there are 2 optional parts.

- The rules that define the set of filters to apply for each client.
- And the process id in term of certificate.

More about it in the [network configuration](networking.html)
