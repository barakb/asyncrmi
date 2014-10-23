package org.async.rmi;

import org.async.rmi.client.RMIInvocationHandler;
import org.async.rmi.modules.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 */
public class DynamicExporter implements Exporter {
    private static final Logger logger = LoggerFactory.getLogger(DynamicExporter.class);
    private final Configuration configuration;

    private Map<WeakKey, WeakReference<Remote>> exportedObjects;

    public DynamicExporter(Configuration configuration) {
        exportedObjects = new HashMap<>();
        this.configuration = configuration;

    }

    @Override
    public synchronized Remote export(Remote impl) {
        if(impl instanceof Exported){
            return  impl;
        }
        WeakKey wk = new WeakKey(impl);
        WeakReference<Remote> value = exportedObjects.get(wk);
        Remote proxy = value != null ? value.get() : null;

        if (proxy != null)
            return proxy;

        proxy = createProxy(impl);

        exportedObjects.put(wk, new WeakReference<>(proxy));

        return proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public boolean unexport() {
        exportedObjects.clear();

		/*
         * always return <code>true</code> because we manage multiple exported
		 * obj per Exporter
		 */
        return true;
    }


    @Override
    synchronized public boolean unexport(Remote obj) {
		/* remove from cache */
        exportedObjects.remove(new WeakKey(obj));

		/* always success status */
        return true;
    }

    private Remote createProxy(Remote impl) {
        Class[] remoteInterfaces = extractRemoteInterfaces(impl.getClass());
        RMIInvocationHandler handler = new RMIInvocationHandler(impl, remoteInterfaces);
        return (Remote) Proxy.newProxyInstance(impl.getClass().getClassLoader(), remoteInterfaces, handler);
    }


    private Class[] extractRemoteInterfaces(Class cls) {
        ArrayList<Class<?>> found = new ArrayList<Class<?>>();
        found.add(Exported.class);
        while (!cls.equals(Object.class)) {
            for (Class cl : cls.getInterfaces()) {
                if (Remote.class.isAssignableFrom(cl)) {
                    found.add(cl);
                }
            }
            cls = cls.getSuperclass();
        }
        return found.toArray(new Class[found.size()]);
    }

    private static class WeakKey
            extends WeakReference<Remote> {
        private int hashCode;
        private String className;

        public WeakKey(Remote obj) {
            super(obj);

            if (obj == null)
                throw new IllegalArgumentException("The Key can't be null");

            className = obj.getClass().getName();
            hashCode = obj.getClass().hashCode() ^ System.identityHashCode(obj);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        /**
         * Returns true if the argument is an instance of the same concrete class,
         * and if or if neither object has had its weak and their values are ==
         * (equals by reference).
         */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof WeakKey))
                return false;

            WeakKey weakKey = (WeakKey) o;
            Object key = weakKey.get();

            return key == get();
        }

        @Override
        public String toString() {
            Object implObj = get();

            if (implObj == null) {
                return "ImplObj: Collected by GC-" + className + "@" + hashCode;
            }

            return "ImplObj: [" + className + "@" + hashCode;
        }
    }
}
