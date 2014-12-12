package org.async.rmi;

import org.async.rmi.config.Configuration;
import org.async.rmi.config.PropertiesReader;
import org.async.rmi.server.MarshalInputStream;
import org.async.rmi.server.MarshalOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class Util {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

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

    public static <T> T readYmlFile(File file, T instance) throws IOException, IllegalAccessException, IntrospectionException, InvocationTargetException, InstantiationException {
        try (InputStream is = new FileInputStream(file)) {
            return readYmlFile(is, instance);
        }
    }


    public static <T> T readYmlContent(String content, T instance) throws IOException, IllegalAccessException, IntrospectionException, InvocationTargetException, InstantiationException {
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            return readYmlFile(is, instance);
        }
    }

    public static <T> T readYmlFile(InputStream is, T instance) throws MalformedURLException, IntrospectionException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(is);
        return read(map, instance);
    }


    private static <T> Stream<T> toStream(Collection<T> collection) {
        if (collection == null) {
            return Stream.empty();
        } else {
            return collection.stream();
        }
    }

    public static <T> T read(Map properties, T instance) throws IntrospectionException, InvocationTargetException, IllegalAccessException, MalformedURLException, InstantiationException {
        return PropertiesReader.read(properties, instance);
    }

    public static Configuration readConfiguration(File ymlFile) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException, IOException {
        return readYmlFile(ymlFile, new Configuration());
    }
    public static Configuration readConfiguration(String configuration) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException, IOException {
        return readYmlContent(configuration, new Configuration());
    }
}
