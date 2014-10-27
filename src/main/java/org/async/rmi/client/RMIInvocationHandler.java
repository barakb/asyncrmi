package org.async.rmi.client;

import org.async.rmi.Configuration;
import org.async.rmi.Modules;
import org.async.rmi.modules.Util;
import org.async.rmi.net.ServerPeer;
import org.async.rmi.server.RemoteRef;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class RMIInvocationHandler implements InvocationHandler, Externalizable, Remote {

    private final transient ClassLoader exporterContextClassLoader;
    private final transient Remote exported;
    private  Class[] remoteInterfaces;
    private RemoteRef ref;

    private Configuration configuration;
    private RemoteObjectAddress remoteObjectAddress;
    private Map<Method, Long> methodToMethodIdMap;


    private long remoteObjectId;


    public RMIInvocationHandler() {
        this.exported = null;
        this.exporterContextClassLoader = null;
    }

    public RMIInvocationHandler(Remote exported, Class[] remoteInterfaces) {
        this.exported = exported;
        this.remoteInterfaces = remoteInterfaces;
        this.exporterContextClassLoader = Thread.currentThread().getContextClassLoader();
        this.methodToMethodIdMap = createMethodToMethodIdMap(remoteInterfaces);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass == Object.class) {
            if (method.getName().equals("hashCode"))
                return hashCode();

            if (method.getName().equals("equals"))
                return equals(args[0]);

            if (method.getName().equals("toString"))
                return toString();

            throw new InternalError("Unexpected Object method dispatched: " + method);
        }
        if (exported != null) {
            return invokeLocally(method, args);
        } else {
            return invokeRemote(proxy, method, args);
        }
    }

    private Object invokeLocally(Method method, Object[] args) throws Throwable {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(exporterContextClassLoader);
            return method.invoke(exported, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private Object invokeRemote(Object proxy, Method method, Object[] args) throws Exception{
        try {
            if (!(proxy instanceof Remote)) {
                throw new IllegalArgumentException(
                        "proxy not Remote instance");
            }
            return ref.invoke((Remote) proxy, method, args, methodToMethodIdMap.get(method));
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                Class<?> cl = proxy.getClass();
                try {
                    method = cl.getMethod(method.getName(),
                            method.getParameterTypes());
                } catch (NoSuchMethodException nsme) {
                    throw (IllegalArgumentException) new IllegalArgumentException().initCause(nsme);
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
            mapping.put(method, util.computeMethodHash(method));
        }
        return mapping;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (exported != null) {
            synchronized (this) {
                if (remoteObjectAddress == null) {
                    export();
                }
            }
        }
        out.writeObject(configuration);
        out.writeObject(remoteInterfaces);
        out.writeObject(ref);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        configuration = (Configuration) in.readObject();
        remoteInterfaces = (Class[]) in.readObject();
        this.ref = (RemoteRef) in.readObject();
        this.methodToMethodIdMap = createMethodToMethodIdMap(remoteInterfaces);
    }

    private void export() throws UnknownHostException {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(exporterContextClassLoader);
            ServerPeer serverPeer = Modules.getInstance().getTransport().export(exported, configuration);
            remoteObjectAddress = new RemoteObjectAddress(serverPeer.getConnectionURL(), serverPeer.getObjectId()
                    , serverPeer.getClassLoaderId());
            ref = Modules.getInstance().getTransport().createUnicastRef(remoteObjectAddress, remoteInterfaces);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
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
        return remoteObjectAddress != null && remoteObjectAddress.equals(handler.remoteObjectAddress);
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
