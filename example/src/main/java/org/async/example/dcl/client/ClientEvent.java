package org.async.example.dcl.client;

import java.io.Serializable;
import java.util.EventObject;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public class ClientEvent extends EventObject{
    private final int counter;

    public ClientEvent(Object source, int counter) {
        super(source);
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return "ClientEvent{" +
                "counter=" + counter +
                '}';
    }
}
