package org.async.rmi.client;

import org.async.rmi.Modules;
import org.async.rmi.messages.Response;
import org.async.rmi.net.ResponseFutureHolder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PendingRequestsTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(PendingRequestsTest.class);

    private PendingRequests pendingRequests;

    @Before
    public void setup() {
        pendingRequests = new PendingRequests();
        Modules.getInstance().getConfiguration().setClientTimeout(1, TimeUnit.MILLISECONDS);
    }

    @Test(timeout = 5000)
    public void testAdd() throws Exception {
        CompletableFuture<Response> pending1 = new CompletableFuture<>();
        CompletableFuture<Response> pending2 = new CompletableFuture<>();
        pendingRequests.add(new ResponseFutureHolder(pending1, null, null), 2);
        pendingRequests.add(new ResponseFutureHolder(pending2, null, null), 3);
        assertThat(pendingRequests.size(), is(2));
        pendingRequests.process(3);
        assertThat(pending1.isCompletedExceptionally(), is(true));
        assertThat(pending2.isCompletedExceptionally(), is(false));
        assertThat(pendingRequests.size(), is(1));
        pendingRequests.process(4);
        assertThat(pending2.isCompletedExceptionally(), is(true));
        assertThat(pendingRequests.size(), is(0));

        CompletableFuture<Response> pending3 = new CompletableFuture<>();
        pendingRequests.add(new ResponseFutureHolder(pending3, null, null), 4);
        pending3.completeExceptionally(new RemoteException());
        //noinspection StatementWithEmptyBody
        while (0 < pendingRequests.size()) {
        }

        CompletableFuture<Response> pending4 = new CompletableFuture<>();
        pendingRequests.add(new ResponseFutureHolder(pending4, null, null), 4);
        pending4.complete(null);
        //noinspection StatementWithEmptyBody
        while (0 < pendingRequests.size()) {
        }

    }
}