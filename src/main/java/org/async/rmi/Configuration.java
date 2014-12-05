package org.async.rmi;

import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private int configurePort = 0;
    private int actualPort;
    private TimeSpan clientConnectTimeout = new TimeSpan(30, TimeUnit.SECONDS);
    private TimeSpan clientTimeout = new TimeSpan(30, TimeUnit.SECONDS);
    private String serverHostName;
    private Factory<SslContext> sslServerContextFactory;
    private Factory<SslContext> sslClientContextFactory;
    private Netmap netmap;

    public Configuration() {
        String netmapFileName = System.getProperty("java.rmi.server.netmapfile", null);
        if(netmapFileName != null){
            try {
                netmap = Netmap.readNetMapFile(new File(netmapFileName));
            }catch(Exception e){
                logger.error("failed to read netmap file {}",netmapFileName,  e);
            }
        }
    }

    public int getConfigurePort() {
        return configurePort;
    }

    public String getServerHostName(){
        if(serverHostName == null){
            return System.getProperty("java.rmi.server.hostname", null);
        }
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
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

    public Factory<SslContext> getSslServerContextFactory() {
        return sslServerContextFactory;
    }

    public void setSslServerContextFactory(Factory<SslContext> sslServerContextFactory) {
        this.sslServerContextFactory = sslServerContextFactory;
    }

    public Factory<SslContext> getSslClientContextFactory() {
        return sslClientContextFactory;
    }

    public void setSslClientContextFactory(Factory<SslContext> sslClientContextFactory) {
        this.sslClientContextFactory = sslClientContextFactory;
    }

    public Netmap getNetmap() {
        return netmap;
    }

    public void setNetmap(Netmap netmap) {
        this.netmap = netmap;
    }
}
