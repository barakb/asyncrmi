package org.async.rmi.config;

import org.async.rmi.TimeSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Barak Bar Orion
 * 12/12/14.
 */
public class PropertiesReader {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    @SuppressWarnings("unchecked")
    public static <T> T read(Map properties, T instance) throws IntrospectionException, InvocationTargetException, IllegalAccessException, MalformedURLException, InstantiationException {
        BeanInfo info =  Introspector.getBeanInfo(instance.getClass());
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Object value = properties.get(propertyDescriptor.getName());

            if(value != null){
                if(value instanceof Map){
                    Class<?> returnType = propertyDescriptor.getReadMethod().getReturnType();
                    if(returnType.equals(TimeSpan.class)){
                          logger.info("foo {}", value);
                        propertyDescriptor.getWriteMethod().invoke(instance, new TimeSpan((int)((Map)value).get("time"), toTimeUnit((String)((Map)value).get("unit"))));
                    }else {
                        Object ins = read((Map) value, returnType.newInstance());
                        propertyDescriptor.getWriteMethod().invoke(instance, ins);
                    }
                }else if(value instanceof Collection){
                    ParameterizedType type = (ParameterizedType)propertyDescriptor.getReadMethod().getGenericReturnType();
                    List container = new ArrayList<>();
                    for (Object v :(Collection)value) {
                        if(v instanceof Map){
                            Class aClass = (Class) type.getActualTypeArguments()[0];
                            container.add(read((Map)v, aClass.newInstance()));
                        }else if(v instanceof Collection){
                            //todo
                        }else{
                            container.add(v);
                        }
                    }
                    propertyDescriptor.getWriteMethod().invoke(instance, container);
                }else {
                    Type type = propertyDescriptor.getReadMethod().getGenericReturnType();
                    if(String.class.getTypeName().equals(type.getTypeName()) && value instanceof String){
                        propertyDescriptor.getWriteMethod().invoke(instance,(String)value);
                    }else if(File.class.getTypeName().equals(type.getTypeName()) && value instanceof String){
                        propertyDescriptor.getWriteMethod().invoke(instance, new File((String)value));
                    }else if(URL.class.getTypeName().equals(type.getTypeName()) && value instanceof String){
                        propertyDescriptor.getWriteMethod().invoke(instance, new URL((String)value));
                    }else if(long.class.getTypeName().equals(type.getTypeName()) && value instanceof Long){
                        propertyDescriptor.getWriteMethod().invoke(instance, (Long)value);
                    }else if(int.class.getTypeName().equals(type.getTypeName()) && value instanceof Integer){
                        propertyDescriptor.getWriteMethod().invoke(instance, (Integer)value);
                    }else if(boolean.class.getTypeName().equals(type.getTypeName()) && value instanceof Boolean){
                        propertyDescriptor.getWriteMethod().invoke(instance, (Boolean)value);
                    }
                }

            }
        }

        return instance;
    }

    private static TimeUnit toTimeUnit(String unit) {
        return TimeUnit.valueOf(unit.toUpperCase());
    }

}
