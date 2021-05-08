package org.async.rmi;

import org.async.rmi.modules.Util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public class RMIUtil implements Util {

    /**
     * Compute the "method hash" of a remote method.  The method hash
     * is a long containing the first 64 bits of the SHA digest from
     * the UTF encoded string of the method name and descriptor.
     */
    @Override
    public long computeMethodHash(Method m) {
        long hash = 0;
        ByteArrayOutputStream sink = new ByteArrayOutputStream(127);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DataOutputStream out = new DataOutputStream(
                    new DigestOutputStream(sink, md));

            String s = getMethodNameAndDescriptor(m);
            out.writeUTF(s);

            // use only the first 64 bits of the digest for the hash
            out.flush();
            byte hasharray[] = md.digest();
            for (int i = 0; i < Math.min(8, hasharray.length); i++) {
                hash += ((long) (hasharray[i] & 0xFF)) << (i * 8);
            }
        } catch (IOException ignore) {
            /* can't happen, but be deterministic anyway. */
            hash = -1;
        } catch (NoSuchAlgorithmException complain) {
            throw new SecurityException(complain.getMessage());
        }
        return hash;
    }


    @Override
    public MarshalledObject[] marshalParams(Object[] params) throws IOException {
        if (params == null) {
            return null;
        }
        MarshalledObject[] res = new MarshalledObject[params.length];
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            res[i] = new MarshalledObject<>(param);
        }
        return res;
    }

    @Override
    public Object[] unMarshalParams(MarshalledObject[] params) throws IOException, ClassNotFoundException {
        if (params == null) {
            return null;
        }
        Object[] res = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            MarshalledObject param = params[i];
            res[i] = param.get();
        }
        return res;
    }


    @Override
    public List<Method> getSortedMethodList(Class[] remoteInterfaces) {
        ArrayList<Method> methodList = new ArrayList<>();
        for (Class<?> cl : remoteInterfaces) {
            methodList.addAll(Arrays.asList(cl.getMethods()));
        }

        methodList.trimToSize();
        Collections.sort(methodList, (m1, m2) -> m1.toString().compareTo(m2.toString()));
        return methodList;
    }

    private String getTypeDescriptor(Class type) {
        if (type.isPrimitive()) {
            if (type == int.class) {
                return "I";
            } else if (type == boolean.class) {
                return "Z";
            } else if (type == byte.class) {
                return "B";
            } else if (type == char.class) {
                return "C";
            } else if (type == short.class) {
                return "S";
            } else if (type == long.class) {
                return "J";
            } else if (type == float.class) {
                return "F";
            } else if (type == double.class) {
                return "D";
            } else if (type == void.class) {
                return "V";
            } else {
                throw new Error("unrecognized primitive type: " + type);
            }
        } else if (type.isArray()) {
            return type.getName().replace('.', '/');
        } else {
            return "L" + type.getName().replace('.', '/') + ";";
        }
    }

    private String getMethodNameAndDescriptor(Method method) {
        StringBuilder desc = new StringBuilder(method.getName());
        desc.append('(');
        Class[] paramTypes = method.getParameterTypes();
        for (Class paramType : paramTypes) {
            desc.append(getTypeDescriptor(paramType));
        }
        desc.append(')');
        Class returnType = method.getReturnType();
        if (returnType == void.class) {
            desc.append('V');
        } else {
            desc.append(getTypeDescriptor(returnType));
        }
        return desc.toString();
    }
}
