package org.async.example.dcl.client;

import org.async.example.dcl.EventListener;
import org.async.rmi.NoAutoExport;
import org.async.rmi.OneWay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.EventObject;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
@NoAutoExport
public class SerializableListener implements EventListener, Serializable{
    private static final Logger logger = LoggerFactory.getLogger(SerializableListener.class);

    private int id;

    public SerializableListener(int id) {
        this.id = id;
    }

    @Override
    public void onEvent(EventObject event) {
        logger.debug("SerializableListener: I am running on the {}", System.getProperty("side"));
        logger.debug("SerializableListener id: {}, call onEvent: {}", id, event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SerializableListener that = (SerializableListener) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
