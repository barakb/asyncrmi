Dynamic class loading
---------------------
- Both server and client run internal HTTP server that serves as a point to load classes to the other side.
- Automatic exporting of the listeners in the ClientImpl, it's enabled by default.
- FilteredListener use @NoAutoExport to suppress automatic export.
- 3 Kinds of listeners: Serialized, Remote and Hybrid (FilteredListener).
- Event method EventListener::onEvent is OneWay that does not wait for the message to be sent (fast).
- For servers and client class server start automatically if "java.rmi.server.codebase" is not set or if it set to number
  (-Djava.rmi.server.codebase=18080) this number is interpreted as the port that the class server should bind to.

  see [example](https://github.com/barakb/asyncrmi/blob/master/example/src/main/java/org/async/example/dcl/client/ClientImpl.java)

## Compile
From the asyncrmi folder type `./build.sh`

# Run
Server `java -jar target/dcl-server.jar` and client `java -jar target/dcl-client.jar`


# Try this
Run fresh server `java -jar target/dcl-server.jar`
Run the client with bogus codebase `java -Djava.rmi.server.codebase="http://google.com" -jar target/dcl-client.jar`
You will see that the server complains about missing class, and throw exception back to the client.

Here is the [doc](http://barakb.github.io/asyncrmi/rmi-pros.html) that describe the idea for this example.