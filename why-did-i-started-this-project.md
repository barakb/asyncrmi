---
layout: post
title:  Why did I started this project
date:   2014-11-01 15:40:56
categories: introduction
---



#Java RMI is grate, here are some of its pros:

- Unlike Corba or Web services it does not need another compilation step.

- It transfer POJO and built on top Java native serialization.

- It can load new code dynamically from http server.

- It has distributed garbage collection.


#Even with those pros Java RMI is not being used as much as it should be and here is (what I think some of the reasons) for that.

- The dynamic code loading is cumbersome to use, 
   you have to run 2 web http server deploy the jars and configure the code base.
   while this is accepted for production it is not accepted for development.

- The implementation shipped with Oracle Java is not production ready, 
  it was written before the age of NIO and hence, uses thread per socket model that is not scale well.

- It does not leverage new Java futures such as:
     + Futures.
     + Closures.
     + Streams.

#Just for example supposed we have server that have to notifiy hundreds of clients remotely about some event.
With current Java RMI implementation it is impossible to write it right, the code contains something like that:

````java
for(Listener listener : listeners){
    listener.notify(event);	     
}
````

But because of the fact that notify is a synchronous remote call that can take arbitrary time (at least until the client finish to process the event) the server can not execute this call in one thread.
In case it does one lazy client or bad network will delay the delivery of the event to all the clients from that point on.


Even if the program use a thread pool to execute the notification the threads can be all exhausted by bad network or disconnected machines, and what size will this thread pool will be anyway ?

#Now lets assume that the remote listener interface could be define in RMI like that:

````java
public interface Listener extends Remote {
    public CompletableFuture<Void> notify(RemoteEvent event);
}
````
Notice that:

1. The notify return Future.
2. The call to notify does not throws RemoteException, this is because the call executing asynchronously.

With this interface you can write the notify code like that:
 
````java
List<CompletableFuture<Void>> pendings = new List<>(listeners.size());

for(Listener listener : listeners){
    // asynchronously notify.
    CompletableFuture<Void> pendingResult = listener.notify(event);
    // register future listenr to cancle the client listener
    // in case of notification failuer.
    pendingResult.exceptionally(throwable -> cancelListener(listener));
    // store the future to be processed by timer thread after the notify timeout expired.
    pendings.add(pendingResult);	     
}
````
At some other time from a timer thread when sufficient time has passed for the notify call to be sent to the client and back to the server call `cancelPending(pendings);`

````java
privat void cancelPending(List<CompletableFuture<Void>> pendings){
  for(CompletableFuture<Void> pending : pendings){
     if(!pending.isDone()){
         pending.cancel();
     }	     
}
````
*Simple and elegant*.


<!--- LocalWords:  NIO notifiy CompletableFuture RemoteException-->
