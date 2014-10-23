package org.async.rmi;

import org.async.rmi.modules.Util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/10/14.
 */
public class RMIUtil implements Util{

    @Override
    public String getMethodNameAndDescriptor(Method method) {
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

    @Override
    public List<Method> getSortedMethodList(Class[] remoteInterfaces){
        ArrayList<Method> methodList = new ArrayList<>();
        for( Class<?> cl : remoteInterfaces){
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
}
