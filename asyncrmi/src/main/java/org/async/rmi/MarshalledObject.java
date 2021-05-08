package org.async.rmi;

import org.async.rmi.server.MarshalInputStream;
import org.async.rmi.server.MarshalOutputStream;

import java.io.*;
import java.util.Arrays;

/**
 * Created by Barak Bar Orion
 * 11/19/14.
 */
public class MarshalledObject<T> implements Externalizable {

    private byte[] bytes;

    public MarshalledObject() {
    }

    public MarshalledObject(T object) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             MarshalOutputStream marshalOutputStream = new MarshalOutputStream(byteArrayOutputStream)) {
            marshalOutputStream.writeObject(object);
            bytes = byteArrayOutputStream.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public T get() throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        MarshalInputStream marshalInputStream = new MarshalInputStream(byteArrayInputStream)){
            return (T)marshalInputStream.readObject();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(bytes);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        bytes = (byte[]) in.readObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarshalledObject that = (MarshalledObject) o;

        return Arrays.equals(bytes, that.bytes);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }


}
