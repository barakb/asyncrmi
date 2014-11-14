master | gh-pages
-------|---------
[![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=master)](https://travis-ci.org/barakb/asyncrmi) | [![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=gh-pages)](https://travis-ci.org/barakb/asyncrmi)

Async RMI is modern Java RMI implementation.


###Feature set (todo add link doc page end link to example dir for done)
- [x] Support asynchronous calls. [documentation](http://barakb.github.io/asyncrmi/asynchronous-calls.html) [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/futures)
  - [x] Java 8 CompletableFuture. [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/futures)
  - [x] Client timeout. [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/timeout)
  - [ ] One way calls. [documentation](http://barakb.github.io/asyncrmi/oneway-calls.html) [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/oneway)
- [ ] Allow configure thread policies. [documentation](http://barakb.github.io/asyncrmi/threads.html) [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/threads)
- [x] Class loading.
  - [x] Code base. [documentation](http://barakb.github.io/asyncrmi/dynamic-class-loading.html) [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/dcl)
  - [ ] Direct class loading.
- [ ] Ease of usage.
  - [x] Maven build.
  - [x] Use Oracle RMI marker interfaces.
    - [x] Remote.
    - [x] RemoteException.
  - [x] Logging [SLF4J](http://www.slf4j.org/)
  - [x] No code generation.
  - [ ] Closure and streams.
  - [ ] Firewalls.
- [ ] DGC.
  - [ ] Leases.
  - [ ] Finalize
- [ ] Performance.
  - [x] Connection pooling.
  - [x] Requests pipeline. [documentation](http://barakb.github.io/asyncrmi/request-pipeline.html) [example](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/pipeline)
  - [ ] Benchmark.
- [x] Logging
  - [x] Add ip addresses. [documentation](http://barakb.github.io/asyncrmi/logging.html)
- [ ] Networking.
    - [x] un publish.
    - [ ] handshake.
    - [ ] port range.
    - [ ] TSL.
- [ ] Client.
   - [ ] Close.


##How to build.
- Install Oracle [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
- Install [maven](http://maven.apache.org/).
- Clone the project `git clone git@github.com:barakb/asyncrmi.git` or download the [zip](https://github.com/barakb/asyncrmi/archive/master.zip) or the [tar.gz](https://github.com/barakb/asyncrmi/archive/master.tar.gz) file. 
- Change dir to the asyncrmi dir and type `mvn install` at the console.

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


The full [documentation](http://barakb.github.io/asyncrmi) page.