---
layout: doc
title:  rmi pros
date:   2014-11-01 15:40:56
categories: introduction
---

- Unlike Corba or Web services it does not need another compilation step.

- It transfer POJO and built on top Java native serialization.

- It can load new code dynamically from http server.

- It has distributed garbage collection.

When those four properties combined together they gives enormous flexibility.
It is best shown with an Example:
Lets assume we have a 3rd party notify server with this API

````java
public interface Server extends Remote {
    void addListener(EventListener listener);
    void removeListener(EventListener listener);
    void triggerEvent(EventObject event);
}
````
Where EventListener is part of the provided api defined as

````java
public interface EventListener extends Remote{
    void onEvent(EventObject event);
}
````

As a client of this server I can:

1. Register my listeners to events.
2. Send my event to all the already registered listeners.

````java
server.addListener(new EventListener(){
    void onEvent(EventObject event){
        logger.info("got an event from the server");
    }
);
````

Or with Java8 lambda syntax

````java
server.addListener(event -> {
    logger.info("got an event from the server");
});
````

Note that in both the client send the server an event with class that exists only on the server.
And that the logger.info is executed at the client this is because the Listener itself is a remote proxy to a server
in the client JVM.

And here is the interaction diagram for this client call.
<div class="diagram">
    Client->Server: addListener(eventListener)
</div>

Client can trigger an event with:

```java
server.triggerEvent(new ClientEvent(someData));
```

Where again ClientEvent is a new class that the server have to download dynamically from the client,
in fact all the other client that register a listener will have to download this new
class definition from the client that send the event.

And here is the interaction diagram for this client call.
<div class="diagram">
    Client->Server: addListener(eventListener)
    Client->Server: triggerEvent(event)
    Server->Client: onEvent(event)
</div>

Note that by sending to the server a remote object (The listener) the client secretly publish
the remote listener as server inside the client JVM and send to the server a proxy for this client.
With this proxy the server send the event to the client.

Util now there was nothing special, RMI pass classes definition behind the scenes and everybody happy.
But lets say for example that the action that we wish to apply on event is performing an HTTP post to some external site.
We can do that! it is easy we just have to post the request when the on event is called.

```java
server.addListener(event -> {
    event.sendPostRequest();
});
server.triggerEvent(new ClientEvent(someURL));
```
And the sequence diagram for that is:
<div class="diagram">
    Client->Server: addListener(eventListener)
    Client->Server: triggerEvent(event)
    Server->Client: onEvent(event)
    Client->SomeURL: post(data)
</div>

Wait you say, this is not optimized, and of course you are right, isn't it better if the server will send the post
message instead of forwarding the event to the client.

The rule for sending RMI method call arguements are:

1. primitives sent as is.
2. Remote objects replaced with a proxy.
3. Serialized object send as Serialized (the other side get a copy).


So in order to for the client to register a listener that its onEvent will be executed on the server it have to:

1. Be a Serialized object.
2. Not be a RemoteObject.

1 is very easy one just have to define his listener implementation as Serializable but how can we we register a non RemoteObject as a listener ?
Remember that the listener interface defined as

```java
public interface EventListener extends Remote
```

Actually we can't do that but we can mark out listener in special mark that will let the RMI system to ignore the remoteness of this object.
To do that asyncrmi introduce the class annotation [`@NoAutoExport`](https://github.com/barakb/asyncrmi/blob/master/src/main/java/org/async/rmi/NoAutoExport.java)
This is the doc for the NoAutoExport

>The @NoAutoExport annotation instruct the underline RMI system to
>treat Object of this class as Serialize rather then Remote although they can implement Remote
>They will never be replaced with a proxy to them in the RMI serialization.

Now we can define a listener that will execute its code in the server like that:


```java
@NoAutoExport
public class SerializableListener implements EventListener, Serializable{
    public SerializableListener() {
    }

    @Override
    public void onEvent(EventObject event) {
        post(event)
    }

    private void pos(EventObject){
      ...
    }
}

```
And have the client code:

```java
server.addListener(new SerializableListener());
server.triggerEvent(new ClientEvent(aURL));
```

The diagram should look like that:

<div class="diagram">
    Client->Server: addListener(eventListener)
    Client->Server: triggerEvent(event)
    Server->SomeURL: post(data)
</div>

Clean and nice without change single line in the client or the server we controlled from what machine will the post sent.

Now lets assume that we are using this notify system and we note that we generally have 3 types of actions as a result of event:

1. Send post request to some url.
2. Send the event back to the client for more processing.
3. Ignore the event.


Can we achieve that without modify the server ?
As it turn out it is not so hard to do when we have the [`@NoAutoExport`](https://github.com/barakb/asyncrmi/blob/master/src/main/java/org/async/rmi/NoAutoExport.java)
in out bag.

What we need to do is pass to the server a Serialized listener that contains an instance variable of RemoteListener.
The Serialized listener will get the event and handle cases 1 and 3 while using its internal remote listener to handle case 2.

Lets write the code:

```java
@NoAutoExport
public class FilteredListener implements EventListener, Serializable{
    private final EventListener remoteListener;

    public FilteredListener() {
        this.remoteListener = new RemoteListener();
    }

    @Override
    public void onEvent(EventObject event) {
        if(shouldPost(event)){
            post(event);
        }else if (shouldSendBack(event)){
            remoteListener.onEvent(event);
        }else{
            // ignore
        }
     }
     private void pos(EventObject){
           ...
     }
}

```

Without even restart the Server we can have this interaction diagram.

<div class="diagram">
    Client->Server: addListener(filteredListener)
    Client->Server: triggerEvent(ignoredEvent)
    Note right of Server: This event is ignored!
    Client->Server: triggerEvent(postEvent)
    Server->SomeURL: post(data)
    Client->Server: triggerEvent(callbackEvent)
    Server->Client: onEvent(callbackEvent)
</div>

Here is [link](https://github.com/barakb/asyncrmi/tree/master/example/src/main/java/org/async/example/dcl) to a very similar code that is part of the asyncrmi examples.



