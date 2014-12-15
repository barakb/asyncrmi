package org.async.rmi;

import java.rmi.RemoteException;

/**
 * Created by Barak Bar Orion
 * 12/15/14.
 */
public class ConstantServer<T> implements Constant<T> {
    private final T value;

    public ConstantServer(T value) {
        this.value = value;
    }

    @Override
    public T getValue() throws RemoteException {
        return value;
    }
}
