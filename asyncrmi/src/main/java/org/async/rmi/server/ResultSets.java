package org.async.rmi.server;

import org.async.rmi.ResultSetCallback;

/**
 * Created by Barak Bar Orion
 * 7/19/15.
 */
public class ResultSets {

    private static final ThreadLocal<ResultSetCallback> callback = new ThreadLocal<>();

    public static <T> ResultSetCallback<T> getCallback(){
        //noinspection unchecked
        return (ResultSetCallback<T>) callback.get();
    }

    static <V> void set(ResultSetCallback<V> c){
        callback.set(c);
    }

    public static void remove(){
        callback.remove();
    }
}
