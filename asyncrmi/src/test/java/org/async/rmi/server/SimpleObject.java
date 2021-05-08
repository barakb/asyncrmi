package org.async.rmi.server;

import java.io.Serializable;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class SimpleObject implements Serializable {

    public SimpleObject() {
    }

    @Override
    public String toString() {
        return "SimpleObject";
    }
}
