master | gh-pages
-------|---------
[![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=master)](https://travis-ci.org/barakb/asyncrmi) | [![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=gh-pages)](https://travis-ci.org/barakb/asyncrmi)

Async RMI is modern Java RMI implementation.


###Todo
- [x] Support asynchronous calls.
  - [x] Java 8 CompletableFuture.
  - [x] Client timeout.
  - [ ] One way calls.
- [ ] Allow configure thread policies.
- [x] Class loading.
  - [x] Code base.
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
  - [x] Requests pipeline.
  - [ ] Benchmark.
- [ ] Logging
  - [ ] Add ip addresses.
- [ ] Networking.
    - [x] un publish.
    - [ ] handshake.
    - [ ] port range.


##How to build.
- Install Oracle [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
- Install [maven](http://maven.apache.org/).
- Clone the project `git clone git@github.com:barakb/asyncrmi.git` or download the [zip](https://github.com/barakb/asyncrmi/archive/master.zip) or the [tar.gz](https://github.com/barakb/asyncrmi/archive/master.tar.gz) file. 
- Change dir to the asyncrmi dir and type `mvn install` at the console.

##An example.

####[The remote interface](https://github.com/barakb/asyncrmi/blob/master/src/test/java/org/async/rmi/Example.java).
```java

public interface Example extends Remote {
    public String echo(String msg) throws RemoteException;
    public CompletableFuture<String> futuredEcho(String msg) throws RemoteException;
}
```

####[The server and the client](https://github.com/barakb/asyncrmi/blob/master/src/test/java/org/async/rmi/ExampleServer.java).
```java
public class ExampleServer implements Example {
    private static final Logger logger = LoggerFactory.getLogger(ExampleServer.class);

    @Override
    public String echo(String msg) throws RemoteException {
        logger.debug("Server: called echo({})", msg);
        return msg;
    }

    @Override
    public CompletableFuture<String> futuredEcho(final String msg) throws RemoteException {
        logger.debug("Server: futuredEcho echo({})", msg);
        final CompletableFuture<String> res = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.toString(), e);
            }
            res.complete(msg);
        }).start();
        return res;
    }

    public static void main(String[] args) throws Exception {
        try {
            ExampleServer server = new ExampleServer();
            Example proxy = (Example) Modules.getInstance().getExporter().export(server);
            File file = new File("ExampleServer.proxy");
            Util.serialize(Files.asByteSink(file), proxy);
            logger.info("proxy {} saved to file  {}, server is running at: {}:{}", proxy, file.getAbsolutePath());
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

The full [documentation](http://barakb.github.io/asyncrmi) page.