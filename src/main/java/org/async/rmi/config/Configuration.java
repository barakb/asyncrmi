package org.async.rmi.config;

import org.async.rmi.TimeSpan;
import org.async.rmi.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Configuration {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private int configurePort = 0;
    private int actualPort;
    private TimeSpan clientConnectTimeout = new TimeSpan(30, TimeUnit.SECONDS);
    private TimeSpan clientTimeout = new TimeSpan(30, TimeUnit.SECONDS);
    private String serverHostName;
    private NetMap netMap;

    public static Configuration readDefault() {
        String ymlFileName = System.getProperty("java.rmi.server.config", "config.yml");
        File ymlFile = new File(ymlFileName);
        if (ymlFile.exists()) {
            logger.debug("reading configuration from {}", ymlFile.getAbsolutePath());
            try {
                return Util.readConfiguration(ymlFile);
            } catch (Exception e) {
                logger.error(e.toString(), e);
                return new Configuration();
            }
        } else {
            return new Configuration();
        }

    }

    public Configuration() {
        this.netMap = NetMap.empty();
    }

    public int getConfigurePort() {
        return configurePort;
    }

    public String getServerHostName() {
        if (serverHostName == null) {
            return System.getProperty("java.rmi.server.hostname", null);
        }
        return serverHostName;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public void setConfigurePort(int configurePort) {
        this.configurePort = configurePort;
    }

    public int getActualPort() {
        return actualPort;
    }

    public void setActualPort(int actualPort) {
        this.actualPort = actualPort;
    }

    public TimeSpan getClientConnectTimeout() {
        return clientConnectTimeout;
    }

    public Configuration setClientConnectTimeout(long time, TimeUnit timeUnit) {
        this.clientConnectTimeout = new TimeSpan(time, timeUnit);
        return this;
    }

    public TimeSpan getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(long time, TimeUnit timeUnit) {
        this.clientTimeout = new TimeSpan(time, timeUnit);
    }

    public NetMap getNetMap() {
        return netMap;
    }

    public void setNetMap(NetMap netMap) {
        this.netMap = netMap;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setClientConnectTimeout(TimeSpan clientConnectTimeout) {
        this.clientConnectTimeout = clientConnectTimeout;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setClientTimeout(TimeSpan clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

}
