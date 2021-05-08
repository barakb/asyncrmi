package org.async.rmi.client;

import org.async.rmi.*;
import org.async.rmi.config.Configuration;
import org.async.rmi.modules.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import java.util.*;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class RMIInvocationHandler implements InvocationHandler, Externalizable, Remote {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(RMIInvocationHandler.class);

    private final transient ClassLoader exporterContextClassLoader;
    private final transient Remote impl;
    private Class[] remoteInterfaces;
    private RemoteRef ref;
    private Map<Long, OneWay> oneWayMap;
    private Set<Long> resultSetSet;
    private Map<Long, Trace> traceMap;

    private transient Configuration configuration;
    private Map<Method, Long> methodToMethodIdMap;


    public RMIInvocationHandler() {
        this.impl = null;
        this.exporterContextClassLoader = null;
    }

    public RMIInvocationHandler(Remote impl, Class[] remoteInterfaces, long objectId) throws InterruptedException, UnknownHostException {
        this.impl = impl;
        this.remoteInterfaces = remoteInterfaces;
        this.exporterContextClassLoader = Thread.currentThread().getContextClassLoader();
        this.methodToMethodIdMap = createMethodToMethodIdMap(remoteInterfaces);
        this.oneWayMap = createOneWayMap();
        this.resultSetSet = createResultSetSet();
        this.traceMap = createTraceMap();
        this.configuration = Modules.getInstance().getConfiguration();
        this.ref = Modules.getInstance().getTransport().export(impl, remoteInterfaces, configuration, oneWayMap, resultSetSet, traceMap, objectId);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        logger.info("invoking {} on {} ", method.getName());
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass == Object.class) {
            if (method.getName().equals("hashCode"))
                return hashCode();

            if (method.getName().equals("equals"))
                return equals(args[0]);

            if (method.getName().equals("toString"))
                return toString();

            throw new InternalError("Unexpected Object method dispatched: " + method);
        } else if (declaringClass == Exported.class) {
            if (method.getName().equals("getObjectId")) {
                return ((UnicastRef) ref).getObjectid();
            }if(method.getName().equals("close")){
                ref.close();
                return null;
            } if(method.getName().equals("redirect")){
                //redirect this proxy to another server.
                //redirect(long objectId, String host, int port)
                ((UnicastRef) ref).redirect((Long)args[0], (String)args[1], (Integer)args[2]);
                return null;
            }
            throw new InternalError("Unexpected Object method dispatched: " + method);
        }
        if (impl != null) {
            return invokeLocally(method, args);
        } else {
            return invokeRemote(proxy, method, args);
        }
    }

    private Object invokeLocally(Method method, Object[] args) throws Throwable {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(exporterContextClassLoader);
            return method.invoke(impl, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private Object invokeRemote(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (!(proxy instanceof Remote)) {
                throw new IllegalArgumentException(
                        "proxy not Remote instance");
            }
            long methodId = methodToMethodIdMap.get(method);
            return ref.invoke((Remote) proxy, method, args, methodId, oneWayMap.get(methodId),
                    resultSetSet.contains(methodId));
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                Class<?> cl = proxy.getClass();
                try {
                    method = cl.getMethod(method.getName(),
                            method.getParameterTypes());
                } catch (NoSuchMethodException nsme) {
                    throw new IllegalArgumentException().initCause(nsme);
                }
                Class<?> thrownType = e.getClass();
                for (Class<?> declaredType : method.getExceptionTypes()) {
                    if (declaredType.isAssignableFrom(thrownType)) {
                        throw e;
                    }
                }
                e = new UnexpectedException("unexpected exception", e);
            }
            throw e;
        }
    }


    private Map<Method, Long> createMethodToMethodIdMap(Class[] remoteInterfaces) {
        Util util = Modules.getInstance().getUtil();
        List<Method> sortedMethodList = util.getSortedMethodList(remoteInterfaces);
        Map<Method, Long> mapping = new HashMap<>(sortedMethodList.size());
        for (Method method : sortedMethodList) {
            long methodId = util.computeMethodHash(method);
            mapping.put(method, methodId);
        }
        return mapping;
    }

    private Map<Long, Trace> createTraceMap(){
        Map<Long, Trace> res = new HashMap<>();
        for (Method method : methodToMethodIdMap.keySet()) {
            Trace trace = impl.getClass().getAnnotation(Trace.class);
            if(trace != null){
                res.put(methodToMethodIdMap.get(method), trace);
                continue;
            }
            trace = getImplAnnotation(impl, method, Trace.class);
            if (trace != null) {
                res.put(methodToMethodIdMap.get(method), trace);
                continue;
            }
            trace = method.getDeclaringClass().getAnnotation(Trace.class);
            if (trace != null) {
                res.put(methodToMethodIdMap.get(method), trace);
                continue;
            }
            trace = method.getAnnotation(Trace.class);
            if (trace != null) {
                res.put(methodToMethodIdMap.get(method), trace);
            }
        }
        return res;
    }

    private Map<Long, OneWay> createOneWayMap() {
        Map<Long, OneWay> res = new HashMap<>();
        for (Method method : methodToMethodIdMap.keySet()) {
            OneWay oneWay = getImplAnnotation(impl, method, OneWay.class);
            if (oneWay == null) {
                oneWay = method.getAnnotation(OneWay.class);
            }
            if (oneWay != null) {
                res.put(methodToMethodIdMap.get(method), oneWay);
            }
        }
        return res;
    }
    private Set<Long> createResultSetSet() {
        HashSet<Long> res = new HashSet<>();
        for (Method method : methodToMethodIdMap.keySet()) {
            if(ResultSet.class.isAssignableFrom(method.getReturnType())){
                res.add(methodToMethodIdMap.get(method));
            }
        }
        return res;
    }

    private <T extends Annotation> T getImplAnnotation(Remote impl, Method method, Class<T> annotation) {
        try {
            return impl.getClass().getMethod(method.getName(), method.getParameterTypes()).getAnnotation(annotation);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(remoteInterfaces);
        out.writeObject(oneWayMap);
        out.writeObject(resultSetSet);
        out.writeObject(traceMap);
        out.writeObject(ref);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        remoteInterfaces = (Class[]) in.readObject();
        this.oneWayMap = (Map<Long, OneWay>) in.readObject();
        this.resultSetSet = (Set<Long>) in.readObject();
        this.traceMap = (Map<Long, Trace>) in.readObject();
        this.ref = (RemoteRef) in.readObject();
        this.methodToMethodIdMap = createMethodToMethodIdMap(remoteInterfaces);
        this.configuration = Modules.getInstance().getConfiguration();
    }


    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        RMIInvocationHandler handler = extractRMIInvocationHandler(obj);

        if (handler == null) {
            return false;
        }

        if (handler == this) {
            return true;
        }

//		/* equals the local obj references if we still in local VM */
//        if ( _localObj != null && eqSt.getLocalObjImpl() != null )
//            return _localObj == eqSt.getLocalObjImpl();

		/* equals by remote objectId */
        return ref != null && ref.equals(handler.ref);
    }

    private RMIInvocationHandler extractRMIInvocationHandler(Object obj) {
        if (Proxy.isProxyClass(obj.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(obj);
            if (invocationHandler instanceof RMIInvocationHandler) {
                return (RMIInvocationHandler) invocationHandler;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "RemoteObject{" + ref + "}@" + hashCode();
    }
}
