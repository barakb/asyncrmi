package org.async.rmi;

import org.async.rmi.modules.Exporter;
import org.async.rmi.modules.Transport;
import org.async.rmi.modules.Util;
import org.async.rmi.net.NettyTransport;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public class Modules {
    private static Modules instance;
    private Configuration configuration;
    private Exporter exporter;
    private Transport transport;
    private Util util;

    private Modules() {
        configuration = new Configuration();
        setExporter(new DynamicExporter(configuration));
        setTransport(new NettyTransport());
        setUtil(new RMIUtil());
    }

    public static synchronized Modules getInstance(){
        if(instance == null){
            instance = new Modules();
        }
        return instance;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Exporter getExporter() {
        return exporter;
    }

    public void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Util getUtil() {
        return util;
    }

    public void setUtil(Util util) {
        this.util = util;
    }
}
