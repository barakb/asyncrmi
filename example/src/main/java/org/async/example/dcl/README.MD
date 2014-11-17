Dynamic class loading
---------------------
- Both server and client run internal HTTP server that serves as a point to load classes to the other side.
- Automatic exporting of the listeners in the ClientImpl, it's enabled by default.
- FilteredListener use @NoAutoExport to suppress automatic export.
- 3 Kinds of listeners: Serialized, Remote and Hybrid (FilteredListener).
- Event method EventListener::onEvent is OneWay that does not wait for the message to be sent (fast).
- For servers class server start automatically if "java.rmi.server.codebase" is not set or if it set to number
  (-Djava.rmi.server.codebase=18080) this number is interpreted as the port that the class server should bind to.
  If there is no server in the VM the user can create explicitly a class server
   ```Java
    ClassLoaderServer classLoaderServer = new ClassLoaderServer(ClientImpl.class.getClassLoader());
   ```
   When done this class server can be closed with
   ```Java
    classLoaderServer.close();
   ```
   Or even better Java8's try with resource syntax can be used:
   ```Java
     try(ClassLoaderServer classLoaderServer = new ClassLoaderServer(ClientImpl.class.getClassLoader())) {
        // put client code here
     }
   ```



  see [example](https://github.com/barakb/asyncrmi/blob/master/example/src/main/java/org/async/example/dcl/client/ClientImpl.java)

## Compile
From the asyncrmi folder type `./build.sh`

# Run
Server `java -jar target/dcl-server.jar` and client `java -jar target/dcl-client.jar`


