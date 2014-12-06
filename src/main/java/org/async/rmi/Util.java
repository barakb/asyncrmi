package org.async.rmi;

import org.async.rmi.server.MarshalInputStream;
import org.async.rmi.server.MarshalOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public static Netmap readNetMapFile(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return readNetMapStream(is);
        }
    }

    public static Netmap readNetMapStream(InputStream is) {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(is);
        //noinspection unchecked
        return new Netmap(extractRules(toStream((List<Map>) map.get("rules"))));
    }

    public static Netmap readNetMapString(String content) throws IOException {
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            return readNetMapStream(is);
        }
    }


    private static List<Netmap.Rule> extractRules(Stream<Map> rules) {
        //noinspection unchecked
        return rules.map(r -> new Netmap.Rule(extractMatch(r.get("match")), (List<String>) r.get("filters"))).collect(Collectors.toList());
    }

    private static Netmap.Rule.Match extractMatch(Object match) {
        return new Netmap.Rule.Match((String) match);
    }


    private static <T> Stream<T> toStream(Collection<T> collection) {
        if (collection == null) {
            return Stream.empty();
        } else {
            return collection.stream();
        }
    }

}
