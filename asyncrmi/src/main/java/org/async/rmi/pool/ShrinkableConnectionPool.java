package org.async.rmi.pool;

import org.async.rmi.Connection;
import org.async.rmi.Factory;
import org.async.rmi.client.ClosedException;
import org.async.rmi.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 27/10/14.
 */
public class ShrinkableConnectionPool implements Pool<Connection<Message>> {
    private static final Logger logger = LoggerFactory.getLogger(ShrinkableConnectionPool.class);

    private final List<Connection<Message>> free;
    private final List<Connection<Message>> all;
    private final int optimalSize;
    private Factory<CompletableFuture<Connection<Message>>> factory;
    private boolean closed = false;

    public ShrinkableConnectionPool(int optimalSize) {
        this.free = new ArrayList<>();
        this.all = new ArrayList<>();
        this.optimalSize = optimalSize;
    }
    public void setFactory(Factory<CompletableFuture<Connection<Message>>> factory) {
        this.factory = factory;
    }

    @Override
    public synchronized CompletableFuture<Connection<Message>> get() {
        if(closed){
            throw new ClosedException();
        }
        if (!free.isEmpty()) {
            return CompletableFuture.completedFuture(free.remove(0));
        } else {
            CompletableFuture<Connection<Message>> result = factory.create();
            result.thenAccept(all::add);
            return result;
        }
    }

    @Override
    public synchronized void free(Connection<Message> c) {
        if (c.isClosed()) {
            all.remove(c);
            free.remove(c);
        }else if (optimalSize < all.size()) {
            try {
                c.close();
                all.remove(c);
            } catch (IOException e) {
                logger.warn(e.toString(), e);
            }
        } else {
            free.add(c);
        }
    }

    int getAllSize() {
        return all.size();
    }

    int getFreeSize() {
        return free.size();
    }

    @Override
    public synchronized void close() throws IOException {
        if(closed){
            return;
        }
        for (Connection<Message> c : all) {
            try {
                c.close();
            } catch (IOException e) {
                logger.warn(e.toString(), e);
            }
        }
        all.clear();
        free.clear();
        closed = true;
    }
}
