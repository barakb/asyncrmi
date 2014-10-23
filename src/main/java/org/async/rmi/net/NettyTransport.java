package org.async.rmi.net;

import org.async.rmi.Configuration;
import org.async.rmi.modules.Transport;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;

/**
 * Created by Barak Bar Orion
 * 06/10/14.
 */
public class NettyTransport implements Transport {

    @Override
    public ServerPeer export(Remote exported, Configuration configuration) throws UnknownHostException {
        final String address = InetAddress.getLocalHost().getHostAddress();
        final int port = 5050;
        return new ServerPeer() {
            @Override
            public String getConnectionURL() {
                return "rmi://" + address + ":" + port;
            }

            @Override
            public long getObjectId() {
                return 8;
            }

            @Override
            public long getClassLoaderId() {
                return 9;
            }
        };
    }
}
