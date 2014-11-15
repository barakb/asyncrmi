package org.async.example.dcl.client;

import org.async.example.dcl.EventListener;
import org.async.rmi.NoAutoExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.EventObject;

/**
 * Created by Barak Bar Orion
 * 11/14/14.
 */
@NoAutoExport
public class FilteredListener implements EventListener, Serializable{
    private static final Logger logger = LoggerFactory.getLogger(FilteredListener.class);
    private int id;

    private final EventListener remoteListener;

    public FilteredListener(int id) {
        this.id = id;
        this.remoteListener = new RemoteListener(id);
    }

    @Override
    public void onEvent(EventObject event) {
        if(event instanceof ClientEvent && (((ClientEvent)event).getCounter() % 2 == 0)){
            logger.debug("FilteredListener: I am running on the {} even events are nice so I will send {} back to the client"
                    , System.getProperty("side"), event);
            remoteListener.onEvent(event);
        }else{
            logger.debug("FilteredListener: I am running on the {} odd events are odd so I will not send {} back to the client"
                    , System.getProperty("side"), event);
        }
     }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilteredListener that = (FilteredListener) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
