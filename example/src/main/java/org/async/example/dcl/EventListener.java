package org.async.example.dcl;

import org.async.rmi.OneWay;

import java.rmi.Remote;
import java.util.EventObject;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
public interface EventListener extends Remote{

    @OneWay(full = true)
    void onEvent(EventObject event);
}
