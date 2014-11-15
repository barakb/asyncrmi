package org.async.example.dcl;

import org.async.rmi.OneWay;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.EventObject;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public interface EventListener extends Remote{

    @OneWay
    void onEvent(EventObject event);
}
