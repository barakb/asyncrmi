package org.async.rmi.server;

import io.netty.channel.ChannelHandlerContext;
import org.async.rmi.ResultSetCallback;
import org.async.rmi.messages.ResultSetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public class ServerResultSetCallback<V> implements ResultSetCallback<V> {
    private static final Logger logger = LoggerFactory.getLogger(ServerResultSetCallback.class);
    private final ChannelHandlerContext ctx;
    final Lock lock = new ReentrantLock();
    final Condition notRequired = lock.newCondition();
    final AtomicBoolean closed = new AtomicBoolean(false);

    public ServerResultSetCallback(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean send(V[] values) {
        lock.lock();
        try {
            ctx.writeAndFlush(new ResultSetResponse(values));
            notRequired.await();
        } catch (InterruptedException e) {
            logger.error(e.toString(), e);
        } finally {
            lock.unlock();
        }
        return closed.get();
    }

    @Override
    public void close() throws Exception {
        if(closed.compareAndSet(false, true)) {
            ctx.attr(ObjectRef.SERVER_RESULT_SET_CALLBACK_ATTRIBUTE_KEY).remove();
            ctx.writeAndFlush(new ResultSetResponse(null));
        }
    }

    public void onClientClosed() {
        if(closed.compareAndSet(false, true)) {
           resume();
        }
    }


    public void resume() {
        lock.lock();
        try {
            notRequired.signal();
        } finally {
            lock.unlock();
        }
    }

}
