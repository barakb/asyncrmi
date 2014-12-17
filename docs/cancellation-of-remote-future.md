---
layout: doc
title:  Cancellation of remote future
date:   2014-11-01 15:40:56
categories: introduction
---

With Async RMI it is possible to cancel a remote future, this trigger cancel request back to the server and cancel the original future.
In the case that the original request was not sent yet to the server the request it will be removed from the queue.

Here is an example canceling a remote future.

```java
public class CancelCounterServer implements CancelCounter {
    private List<CompletableFuture<Void>> futures;

    public CancelCounterServer() {
        futures = new ArrayList<>();
    }

    @Override
    public CompletableFuture<Void> get() {
        CompletableFuture<Void> res = new CompletableFuture<>();
        futures.add(res);
        res.exceptionally(throwable -> {
            if (throwable instanceof CancellationException) {
                futures.remove(res);
            }
            return null;
        });
        return res;
    }

    @Override
    public int getFutures() {
        return futures.size();
    }
}
```
Now suppose you have a client to such server the following code is not an infinite loop.

```java
    CompletableFuture<Void> future = client.get();
    future.cancel(true);
    do {
        Thread.sleep(20);
    } while (0 < client.getFutures());
```