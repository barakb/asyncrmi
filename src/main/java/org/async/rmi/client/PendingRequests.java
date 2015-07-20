package org.async.rmi.client;

import org.async.rmi.Modules;
import org.async.rmi.TimeoutException;
import org.async.rmi.messages.InvokeRequest;
import org.async.rmi.messages.Response;
import org.async.rmi.net.ResponseFutureHolder;

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

    public void add(ResponseFutureHolder responseFutureHolder) {
        this.add(responseFutureHolder, System.currentTimeMillis());
    }

    /**
     * Use for unit tests only.
     */
    void add(ResponseFutureHolder responseFutureHolder, long currentTime) {
        CompletableFuture<Response> future = responseFutureHolder.getResponseFuture();
        PendingRequest pendingRequest = new PendingRequest(future, responseFutureHolder.getInvokeRequest(), currentTime);
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
                pendingRequest.future.completeExceptionally(new TimeoutException(String.valueOf(pendingRequest.getInvokeRequest())));
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
        private final InvokeRequest invokeRequest;
        public final long requestTime;

        public PendingRequest(CompletableFuture<Response> future, InvokeRequest invokeRequest, long requestTime) {
            this.future = future;
            this.invokeRequest = invokeRequest;
            this.requestTime = requestTime;
        }

        public InvokeRequest getInvokeRequest() {
            return invokeRequest;
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



