package org.async.rmi;

import java.io.*;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Util {

    public static <T> T writeAndRead(T object) throws IOException, ClassNotFoundException {
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(object);
            bytes = byteArrayOutputStream.toByteArray();
        }
        try (InputStream is = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(is)) {
            //noinspection unchecked
            return (T) ois.readObject();
        }


    }
}
