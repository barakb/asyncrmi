master | gh-pages
-------|---------
[![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=master)](https://travis-ci.org/barakb/asyncrmi) | [![Build Status](https://travis-ci.org/barakb/asyncrmi.svg?branch=gh-pages)](https://github.com/barakb/asyncrmi/tree/gh-pages)

asyncrmi (RMI Future)
=====================

#Goals

A Modern Java Asynchronous RMI Implementation

- Support asynchronous calls.
    * Java 8 CompletableFuture.
- Support multiple threads policies.
    * NIO.
- Easy To use.
    * Easy class loading.
    * No code generation.
    * Use Oracle RMI marker interfaces when possible.
    * Closures on top of futures and streams.
- Easy to read, understand and debug.
     * Use 3rd parties such as:
        + [netty](http://netty.io/)
        + [slf4j](http://www.slf4j.org/)
- Production ready.
- High performance.


##How to build.

- Install maven (2 or 3)
- Clone the project 'git clone git@github.com:barakb/asyncrmi.git'
- Type 'cd asyncrmi; maven install' in the console.

##Example.

```java

public interface Example extends Remote {
    public String echo(String msg) throws RemoteException;
    public CompletableFuture<String> futuredEcho(String msg) throws RemoteException;
}

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