package org.async.rmi;

import org.async.rmi.client.RMIInvocationHandler;
import org.async.rmi.modules.Exporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Barak Bar Orion
 * 05/10/14.
 * Export Remote Object using DynamicProxy.
 */
public class DynamicExporter implements Exporter {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(DynamicExporter.class);

    private Map<Long, Remote> exportedObjects;

    public DynamicExporter() {
        exportedObjects = new ConcurrentHashMap<>();
    }

    @Override
    public Remote export(Remote impl) throws InterruptedException, UnknownHostException {
        if (impl instanceof Exported) {
            return impl;
        }
        Modules.getInstance().getTransport().listen(impl.getClass().getClassLoader());

        Remote proxy = createProxy(impl);

        exportedObjects.put(((Exported) proxy).getObjectId(), proxy);

        return proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unexport() {
        for (Long objectId : exportedObjects.keySet()) {
            exportedObjects.remove(objectId);
            Modules.getInstance().getObjectRepository().remove(objectId);
        }
        return true;
    }


    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public boolean unexport(Remote obj) {
        if (obj instanceof Exported) {
            long objectId = ((Exported) obj).getObjectId();
            if (obj.equals(exportedObjects.get(objectId))) {
                exportedObjects.remove(objectId);
                Modules.getInstance().getObjectRepository().remove(objectId);
            }
        }
        return true;
    }

    private Remote createProxy(Remote impl) throws UnknownHostException, InterruptedException {
        Class[] remoteInterfaces = extractRemoteInterfaces(impl.getClass());
        RMIInvocationHandler handler = new RMIInvocationHandler(impl, remoteInterfaces);
        return (Remote) Proxy.newProxyInstance(impl.getClass().getClassLoader(), remoteInterfaces, handler);
    }


    private Class[] extractRemoteInterfaces(Class cls) {
        ArrayList<Class<?>> found = new ArrayList<>();
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
}
