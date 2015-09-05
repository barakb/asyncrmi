package org.async.example.dcl;

import java.rmi.Remote;
import java.util.EventObject;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public interface Server extends Remote {
    String SER_FILE_NAME = "dcl.server.ser";
    void addListener(EventListener listener);
    void removeListener(EventListener listener);
    void triggerEvent(EventObject event);
}
