package org.async.rmi.client;

import org.async.rmi.Modules;
import org.async.rmi.TimeoutException;
import org.async.rmi.messages.Response;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Barak Bar Orion
 * 11/2/14.
 */
public class PendingRequests {
    private final ConcurrentLinkedQueue<PendingRequest> requests;


    public PendingRequests() {
        requests = new ConcurrentLinkedQueue<>();
    }

    public void add(CompletableFuture<Response> future) {
        this.add(future, System.currentTimeMillis());
    }

    /**
     * Use for unit tests only.
     */
    void add(CompletableFuture<Response> future, long currentTime) {
        PendingRequest pendingRequest = new PendingRequest(future, currentTime);
        requests.add(pendingRequest);
        future.whenComplete((response, throwable) -> {
            if (null == throwable || !(throwable instanceof TimeoutException)) {
                requests.remove(pendingRequest);
            }
        });
    }

    /**
     * Process the pending request and timeout all the unresolved request that are too old.
     */
    public void process() {
        process(System.currentTimeMillis());
    }

    void process(long currentTime) {
        long timeout = Modules.getInstance().getConfiguration().getClientTimeout().asMilliseconds();
        Iterator<PendingRequest> iterator = requests.iterator();
        while (iterator.hasNext()) {
            PendingRequest pendingRequest = iterator.next();
            if (timeout <= currentTime - pendingRequest.requestTime) {
                pendingRequest.future.completeExceptionally(new TimeoutException());
                iterator.remove();
            }else{
                return;
            }
        }
    }

    /**
     * Use for unit tests only.
     */
    int size(){
        return requests.size();
    }


    private static class PendingRequest {
        public final CompletableFuture<Response> future;
        public final long requestTime;

        public PendingRequest(CompletableFuture<Response> future, long requestTime) {
            this.future = future;
            this.requestTime = requestTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PendingRequest that = (PendingRequest) o;
            return requestTime == that.requestTime && future.equals(that.future);

        }

        @Override
        public int hashCode() {
            int result = future.hashCode();
            result = 31 * result + (int) (requestTime ^ (requestTime >>> 32));
            return result;
        }
    }

}



