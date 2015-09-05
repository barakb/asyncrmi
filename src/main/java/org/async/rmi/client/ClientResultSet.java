package org.async.rmi.client;

import org.async.rmi.Connection;
import org.async.rmi.ResultSet;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.ResultSetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public class ClientResultSet<V> implements ResultSet<V> {
    private static final Logger logger = LoggerFactory.getLogger(ClientResultSet.class);

    private final CompletableFuture<Connection<Message>> connectionFuture;
    private final List<V[]> values;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final CompletableFuture<Void> readyFuture;
    private int index = -1;
    final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();

    public ClientResultSet(CompletableFuture<Connection<Message>> connectionFuture) {
        this.connectionFuture = connectionFuture;
        values = new LinkedList<>();
        this.readyFuture = new CompletableFuture<>();
    }

    @Override
    public boolean next() {
        lock.lock();
        try {
            index += 1;
            if (hasElement()) {
                return true;
            } else {
                connectionFuture.get().send(new ResultSetRequest());
                while (!closed.get() && !hasElement()) {
                    notEmpty.await();
                }
                return !closed.get();
            }
        } catch (Throwable e) {
            logger.error(e.toString(), e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    private boolean hasElement() {
        if (!values.isEmpty() && values.get(0).length <= index) {
            values.remove(0);
            index = 0;
        }
        return !values.isEmpty() && index < values.get(0).length;
    }

    @Override
    public V get() {
        return values.get(0)[index];
    }

    @Override
    public void close() throws Exception {
        close(true);
    }

    private void close(boolean fromClient) throws Exception {
        if (closed.compareAndSet(false, true)) {
            if (fromClient) {
                connectionFuture.get().send(new ResultSetRequest(true));
            }
            connectionFuture.get().clearAttachment();
            connectionFuture.get().free();
            if (!readyFuture.isDone()) {
                readyFuture.complete(null);
            }
        }
    }

    public void onConnectionClosed() {
        try {
            close(false);
        }catch(Exception ignored){

        }
    }

    public void feed(Object value) {
        lock.lock();
        try {
            if (value == null) {
                try {
                    close();
                } catch (Throwable t) {
                    logger.error(t.toString(), t);
                }
            }
            //noinspection unchecked
            values.add((V[]) value);
            if (!readyFuture.isDone()) {
                readyFuture.complete(null);
            }
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public CompletableFuture<Void> readyFuture() {
        return readyFuture;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close(true);
    }
}
