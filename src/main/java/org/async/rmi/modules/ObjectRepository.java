package org.async.rmi.modules;

import org.async.rmi.server.ObjectRef;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class ObjectRepository {

    private AtomicLong nextObjectId = new AtomicLong(0);
    private ConcurrentHashMap<Long, ObjectRef> map = new ConcurrentHashMap<>();

    public long add(ObjectRef ref){
        long objectId = nextObjectId.getAndDecrement();
        map.put(objectId, ref);
        return objectId;
    }

    public ObjectRef get(long objectId){
        return map.get(objectId);
    }
    public ObjectRef remove(long objectId){
        return map.remove(objectId);
    }
}
