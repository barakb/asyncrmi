package org.async.example.dcl.client;

import org.async.example.dcl.EventListener;
import org.async.rmi.Trace;
import org.async.rmi.TraceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventObject;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
@Trace(TraceType.DETAILED)
public class RemoteListener implements EventListener {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(RemoteListener.class);

    private int id;

    public RemoteListener(int id) {
        this.id = id;
    }

    @Override
    public void onEvent(EventObject event) {
        logger.debug("RemoteListener: I am running on the {} got event {}", System.getProperty("side"), event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteListener that = (RemoteListener) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
