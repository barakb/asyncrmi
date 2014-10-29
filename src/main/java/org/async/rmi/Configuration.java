package org.async.rmi;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Configuration {
    private int configurePort = 0;
    private int actualPort;

    public Configuration() {
    }

    public int getConfigurePort() {
        return configurePort;
    }

    public Configuration setConfigurePort(int configurePort) {
        this.configurePort = configurePort;
        return this;
    }

    public int getActualPort() {
        return actualPort;
    }

    public Configuration setActualPort(int actualPort) {
        this.actualPort = actualPort;
        return this;
    }
}
