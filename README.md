master | gh-pages
-------|---------
[![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=master)](https://travis-ci.org/barakb/asyncrmi) | [![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=gh-pages)](https://travis-ci.org/barakb/asyncrmi)

Async RMI is modern true asynchronous Java RMI implementation.

The full [documentation](http://barakb.github.io/asyncrmi/docs/index.html) page.


##Use with Maven dependency.

```xml
    <dependency>
      <groupId>com.github.barakb</groupId>
      <artifactId>asyncrmi</artifactId>
      <version>1.0.3</version>
    </dependency>
```

Alternatively you can use one jar with all dependencies.

```xml
    <dependency>
      <groupId>com.github.barakb</groupId>
      <artifactId>asyncrmi-dep</artifactId>
      <version>1.0.3</version>
    </dependency>
```

See [sample](https://github.com/barakb/asyncrmi-dep-example) project on github.



##How to build.
- Install Oracle [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
- Install [maven](http://maven.apache.org/).
- Clone the project `git clone git@github.com:barakb/asyncrmi.git` or download the [zip](https://github.com/barakb/asyncrmi/archive/master.zip) or the [tar.gz](https://github.com/barakb/asyncrmi/archive/master.tar.gz) file. 
- Change dir to the asyncrmi directory and type `mvn install` at the console.

Alternatively you can get (or build) a Docker image with the dev env:

Download the pre compiled docker image:

1. sudo docker pull barakb/asyncrmi
2. sudo docker run -i -t --name=asyncrmi  barakb/asyncrmi /bin/bash
3. git pull --rebase
4. mvn install

Download the DockerFile from [github](https://github.com/barakb/docker-asyncrmi)


## How to build the examples.
- After building Async RMI from the Async RMI dir type `(cd example; mvn install)`

##An example.

####[The remote interface](https://github.com/barakb/asyncrmi/blob/master/example/src/main/java/org/async/example/embedded/Example.java).
```java

public interface Example extends Remote {
    public String echo(String msg) throws RemoteException;
    public CompletableFuture<String> futuredEcho(String msg) throws RemoteException;
}
```

####[The server and the client](https://github.com/barakb/asyncrmi/blob/master/example/src/main/java/org/async/example/embedded/ExampleServer.java).
```java
public class ExampleServer implements Example {
    private static final Logger logger = LoggerFactory.getLogger(ExampleServer.class);

    @Override
    public String echo(String msg) throws RemoteException {
        logger.debug("Server: called echo({})", msg);
        return msg;
    }

    @Override
    public CompletableFuture<String> futuredEcho(final String msg)
            throws RemoteException {
        logger.debug("Server: futuredEcho echo({})", msg);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error(e.toString(), e);
            }
            return msg;
        });
    }

    public static void main(String[] args) throws Exception {
        try {
            ExampleServer server = new ExampleServer();
            Example proxy = (Example) Modules.getInstance().getExporter().export(server);
            File file = new File("ExampleServer.proxy");
            Util.serialize(Files.asByteSink(file), proxy);
            logger.info("proxy {} saved to file  {}, server is running at: {}:{}",
                    proxy, file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("ExampleServer exception while exporting:", e);
        }

        File file = new File("ExampleServer.proxy");
        //noinspection UnusedDeclaration
        Example example = (Example) Util.deserialize(Files.asByteSource(file));
        String res = example.echo("foo");
        logger.info("client got: {}", res);
        res = example.echo("foo1");
        logger.info("client got: {}", res);

        CompletableFuture<String> future = example.futuredEcho("async foo");
        res = future.join();
        logger.debug("client got async res : {}", res);
    }
}
```

####And the output is:
```
2014-11-07 20:35:04 INFO  NettyTransport:133 - RMI server started: /0:0:0:0:0:0:0:0:46770.
2014-11-07 20:35:04 INFO  ExampleServer:45 - proxy RemoteObject{UnicastRef{remoteObjectAddress=RemoteObjectAddress{url='rmi://127.0.1.1:46770', objectId=0}, remoteInterfaces=[interface org.async.rmi.Exported, interface org.async.example.embedded.Example]}}@1310540333 saved to file  /home/barakbo/opensource/asyncrmi/ExampleServer.proxy, server is running at: {}:{}
2014-11-07 20:35:04 DEBUG UnicastRef:83 - 127.0.0.1:52244 --> 127.0.1.1:46770 : Request [echo] {requestId=0, objectId=0, methodId=5525131960618330777, params=[foo]}
2014-11-07 20:35:04 DEBUG ExampleServer:20 - Server: called echo(foo)
2014-11-07 20:35:04 DEBUG ObjectRef:63 - 127.0.1.1:46770 --> 127.0.0.1:52244 : Response [echo] {requestId=0, result=foo, error=null}
2014-11-07 20:35:04 INFO  ExampleServer:54 - client got: foo
2014-11-07 20:35:04 DEBUG UnicastRef:83 - 127.0.0.1:52245 --> 127.0.1.1:46770 : Request [echo] {requestId=1, objectId=0, methodId=5525131960618330777, params=[foo1]}
2014-11-07 20:35:04 DEBUG ExampleServer:20 - Server: called echo(foo1)
2014-11-07 20:35:04 DEBUG ObjectRef:63 - 127.0.1.1:46770 --> 127.0.0.1:52245 : Response [echo] {requestId=1, result=foo1, error=null}
2014-11-07 20:35:04 INFO  ExampleServer:56 - client got: foo1
2014-11-07 20:35:04 DEBUG UnicastRef:83 - 127.0.0.1:52246 --> 127.0.1.1:46770 : Request [futuredEcho] {requestId=2, objectId=0, methodId=2725453114525883975, params=[async foo]}
2014-11-07 20:35:04 DEBUG ExampleServer:26 - Server: futuredEcho echo(async foo)
2014-11-07 20:35:14 DEBUG ObjectRef:63 - 127.0.1.1:46770 --> 127.0.0.1:52246 : Response [futuredEcho] {requestId=2, result=async foo, error=null}
2014-11-07 20:35:14 DEBUG ExampleServer:60 - client got async res : async foo
```
[The example code](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/embedded).


