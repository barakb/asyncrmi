package org.async.rmi;

import java.io.Serializable;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Configuration implements Serializable {
    private int port = 5050;

    public Configuration() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
