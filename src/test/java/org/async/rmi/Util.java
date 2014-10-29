package org.async.rmi;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;

import java.io.*;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Util {
    public static void serialize(ByteSink byteSink, Object object) throws IOException {
        try (OutputStream bos = byteSink.openStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
        }
    }

    public static Object deserialize(ByteSource byteSource) throws IOException, ClassNotFoundException {
        try (InputStream is = byteSource.openStream(); ObjectInputStream ois = new ObjectInputStream(is)) {
            return ois.readObject();
        }
    }

    public static <T> T writeAndRead(T object) throws IOException, ClassNotFoundException {
        byte [] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(byteArrayOutputStream)) {
            out.writeObject(object);
            bytes = byteArrayOutputStream.toByteArray();
        }
        try (InputStream is = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(is)) {
            //noinspection unchecked
            return (T)ois.readObject();
        }


    }
}
