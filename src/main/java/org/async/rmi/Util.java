package org.async.rmi;

import org.async.rmi.server.MarshalInputStream;
import org.async.rmi.server.MarshalOutputStream;

import java.io.*;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Util {

    public static <T> T writeAndRead(T object) throws IOException, ClassNotFoundException {
        byte[] bytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); MarshalOutputStream out = new MarshalOutputStream(byteArrayOutputStream)) {
            out.writeObject(object);
            bytes = byteArrayOutputStream.toByteArray();
        }
        try (InputStream is = new ByteArrayInputStream(bytes); MarshalInputStream ois = new MarshalInputStream(is)) {
            //noinspection unchecked
            return (T) ois.readObject();
        }
    }

    public static byte[] asByteArray(InputStream in) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = in.read(data, 0, data.length)) != -1) {
                out.write(data, 0, nRead);
            }

            out.flush();
            return out.toByteArray();
        }


    }

    public static void writeToFile(Object object, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file); MarshalOutputStream out = new MarshalOutputStream(fileOutputStream)) {
            out.writeObject(object);
        }
    }

    public static Object readFromFile(File file) throws IOException, ClassNotFoundException {
        try (FileInputStream fileInputStream = new FileInputStream(file); MarshalInputStream in = new MarshalInputStream(fileInputStream)) {
            return in.readObject();
        }
    }
}
