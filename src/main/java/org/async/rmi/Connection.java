package org.async.rmi;

import org.async.rmi.messages.Response;
import org.async.rmi.pool.PooledResource;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public interface Connection<M> extends PooledResource {

    String getRemoteAddress();

    String getLocalAddress();

    void attach(Object value) throws InterruptedException;

    Object attach() throws InterruptedException;

    void clearAttachment() throws InterruptedException;

    void send(M message);

    void sendOneWay(M message, CompletableFuture<Response> response);
}
