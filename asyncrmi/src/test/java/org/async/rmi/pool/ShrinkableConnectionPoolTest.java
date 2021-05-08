package org.async.rmi.pool;

import org.async.rmi.Connection;
import org.async.rmi.messages.Message;
import org.async.rmi.messages.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.core.Is.is;

public class ShrinkableConnectionPoolTest {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ShrinkableConnectionPoolTest.class);

    private ShrinkableConnectionPool pool;


    @Before
    public void setup() {
        pool = new ShrinkableConnectionPool(1);
        pool.setFactory(() -> CompletableFuture.completedFuture(new Connection<Message>() {
            private volatile boolean closed = false;

            @Override
            public void send(Message message) {

            }

            @Override
            public void sendOneWay(Message message, CompletableFuture<Response> response) {
            }

            @Override
            public void close() throws IOException {
                closed = true;
                pool.free(this);
            }

            @Override
            public boolean isClosed() {
                return closed;
            }

            @Override
            public void free() {
                pool.free(this);
            }

            @Override
            public String getRemoteAddress() {
                return null;
            }

            @Override
            public String getLocalAddress() {
                return null;
            }

            @Override
            public void attach(Object value) throws InterruptedException {
            }

            @Override
            public Object attach() throws InterruptedException {
                return null;
            }

            @Override
            public void clearAttachment() throws InterruptedException {

            }
        }));
    }

    @Test(timeout = 5000)
    public void testGet() throws Exception {
        Assert.assertThat(pool.getAllSize(), is(0));
        Assert.assertThat(pool.getFreeSize(), is(0));
        CompletableFuture<Connection<Message>> c1 = pool.get();
        Assert.assertThat(pool.getAllSize(), is(1));
        Assert.assertThat(pool.getFreeSize(), is(0));
        CompletableFuture<Connection<Message>> c2 = pool.get();
        Assert.assertThat(pool.getAllSize(), is(2));
        Assert.assertThat(pool.getFreeSize(), is(0));
        CompletableFuture<Connection<Message>> c3 = pool.get();
        Assert.assertThat(pool.getAllSize(), is(3));
        Assert.assertThat(pool.getFreeSize(), is(0));
        c1.get().close();
        Assert.assertThat(pool.getAllSize(), is(2));
        Assert.assertThat(pool.getFreeSize(), is(0));
        c2.get().free();
        Assert.assertThat(pool.getAllSize(), is(1));
        Assert.assertThat(pool.getFreeSize(), is(0));
        Assert.assertThat(c2.get().isClosed(), is(true));
        pool.free(c3.get());
        Assert.assertThat(pool.getAllSize(), is(1));
        Assert.assertThat(pool.getFreeSize(), is(1));
        Assert.assertThat(c3.get().isClosed(), is(false));
    }
}