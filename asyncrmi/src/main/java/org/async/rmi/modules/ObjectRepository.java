package org.async.rmi.modules;

import org.async.rmi.server.ObjectRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Barak Bar Orion
 * 28/10/14.
 */
public class ObjectRepository {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(ObjectRepository.class);

    private AtomicLong nextObjectId = new AtomicLong(0);
    private ConcurrentHashMap<Long, ObjectRef> map = new ConcurrentHashMap<>();

    public synchronized long add(ObjectRef ref, long objectId){
        if(objectId < 0) {
            if(map.get(objectId) == null){
                map.put(objectId, ref);
                return objectId;
            }else{
                logger.error("{} Overriding existing object {}", ref, map.get(objectId));
                map.put(objectId, ref);
                return objectId;
            }
        }

        for (Map.Entry<Long, ObjectRef> entry : map.entrySet()) {
            if(entry.getValue().equals(ref)){
                return entry.getKey();
            }
        }
        objectId = nextObjectId.getAndDecrement();
        map.put(objectId, ref);
        return objectId;
    }

    public ObjectRef get(long objectId){
        return map.get(objectId);
    }
    public synchronized ObjectRef remove(long objectId){
        return map.remove(objectId);
    }
}
