package org.async.rmi.server;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class SelectiveClassLoader extends URLClassLoader{
    private Set<String> ignoreSet = new HashSet<>();

    @SuppressWarnings("UnusedDeclaration")
    public SelectiveClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @SuppressWarnings("UnusedDeclaration")
    public SelectiveClassLoader(URL[] urls) {
        super(urls);
    }

    @SuppressWarnings("UnusedDeclaration")
    public SelectiveClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(ignoreSet.contains(name)){
            throw new ClassNotFoundException(name);
        }
        return super.loadClass(name);
    }

    public void addIgnore(String name){
        ignoreSet.add(name);
    }
    public void removeIgnore(String name){
        ignoreSet.remove(name);
    }
}
