package org.async.rmi.server;

import java.io.Serializable;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class SimpleObject implements Serializable {
    private int value;

    public SimpleObject(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SimpleObject{" +
                "value=" + value +
                '}';
    }
}
