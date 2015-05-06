package org.async.rmi.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Barak Bar Orion
 * 11/4/14.
 */
public class LoaderHandler {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(LoaderHandler.class);

    private static String codebaseProperty = null;

    static {
        loadCodeBaseProperty();
    }

    private static final HashMap<LoaderKey, LoaderEntry> loaderTable = new HashMap<>();

    /**
     * reference queue for cleared class loader entries
     */
    private static final ReferenceQueue<Loader> refQueue = new ReferenceQueue<>();


    public static void loadCodeBaseProperty() {
        String prop = System.getProperty("java.rmi.server.codebase", null);
        if (prop != null && prop.trim().length() > 0) {
            // normalize the string to allow better caching.
            codebaseProperty = Stream.of(prop.trim().split("\\s+")).map(String::trim).collect(Collectors.joining(" "));
        } else {
            codebaseProperty = null;
        }
    }

    private static URL[] codebaseURLs = null;

    private static final ConcurrentHashMap<String, URL[]> pathToURLsCache = new ConcurrentHashMap<>();

    /**
     * table of class loaders that use codebase property for annotation
     */
    private static final Map<ClassLoader, Void> codebaseLoaders =
            Collections.synchronizedMap(new IdentityHashMap<>());

    static {
        for (ClassLoader codebaseLoader = ClassLoader.getSystemClassLoader(); codebaseLoader != null;
             codebaseLoader = codebaseLoader.getParent()) {
            codebaseLoaders.put(codebaseLoader, null);
        }
    }

    private LoaderHandler() {
    }

    public static String getClassAnnotation(Class<?> cl) {
        String name = cl.getName();
        int nameLength = name.length();
        if (nameLength > 0 && name.charAt(0) == '[') {
            int i = 1;
            while (nameLength > i && name.charAt(i) == '[') {
                i++;
            }
            if (nameLength > i && name.charAt(i) != 'L') {
                return null;
            }
        }

        ClassLoader loader = cl.getClassLoader();
        if (loader == null || codebaseLoaders.containsKey(loader)) {
            return codebaseProperty;
        }

        String annotation = null;
        if (loader instanceof Loader) {
            annotation = ((Loader) loader).getClassAnnotation();
        } else if (loader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) loader).getURLs();
            if (urls != null) {
                annotation = urlsToPath(urls);
            }
        }

        if (annotation != null) {
            return annotation;
        } else {
            return codebaseProperty;
        }
    }

    private static synchronized URL[] getDefaultCodebaseURLs() throws MalformedURLException {
        if (codebaseURLs == null) {
            if (codebaseProperty != null) {
                codebaseURLs = pathToURLs(codebaseProperty);
            } else {
                codebaseURLs = new URL[0];
            }
        }
        return codebaseURLs;
    }

    private static URL[] pathToURLs(String path) throws MalformedURLException {
        URL[] urls = pathToURLsCache.get(path);
        if (urls == null) {
            urls = Stream.of(path.split("\\s+")).map(LoaderHandler::toUrl).filter(url -> url != null).toArray(URL[]::new);
            pathToURLsCache.putIfAbsent(path, urls);
        }
        return urls;
    }

    private static String urlsToPath(URL[] urls) {
        if (urls == null || urls.length == 0) {
            return null;
        }
        return Stream.of(urls).map(URL::toExternalForm).collect(Collectors.joining(" "));
    }

    private static URL toUrl(String token) {
        try {
            return new URL(token);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Load a class from a network location (one or more URLs),
     * but first try to resolve the named class through the given
     * "default loader".
     */
    public static Class loadClass(String codebase, String name, ClassLoader defaultLoader)
            throws MalformedURLException, ClassNotFoundException {
        URL[] urls;
        if (codebase != null) {
            urls = pathToURLs(codebase);
        } else {
            urls = getDefaultCodebaseURLs();
        }

        if (defaultLoader != null) {
            try {
                Class c = Class.forName(name, false, defaultLoader);
                logger.debug("class {} found via defaultLoader, defined by {}", name, c.getClassLoader());
                return c;
            } catch (ClassNotFoundException ignored) {
            }
        }

        return loadClass(urls, name);
    }

    /**
     * Load a class from the RMI class loader corresponding to the given
     * codebase URL path in the current execution context.
     */
    private static Class loadClass(URL[] urls, String name) throws ClassNotFoundException {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        /*
         * Get or create the RMI class loader for this codebase URL path
         * and parent class loader pair.
         */
        Loader loader = lookupLoader(urls, parent);
        try {
            Class c = Class.forName(name, false, loader);
            logger.debug("class {} found via codebase {} , defined by {}", name, Arrays.toString(urls), c.getClassLoader());
            return c;
        } catch (ClassNotFoundException e) {
            logger.debug("class {} not found via codebase", name);
            throw e;
        }
    }

    /**
     * Look up the RMI class loader for the given codebase URL path
     * and the given parent class loader.  A new class loader instance
     * will be created and returned if no match is found.
     */
    private static Loader lookupLoader(final URL[] urls,
                                       final ClassLoader parent) {
        /*
         * If the requested codebase URL path is empty, the supplied
         * parent class loader will be sufficient.
         */

        LoaderEntry entry;
        Loader loader;

        synchronized (LoaderHandler.class) {
            /*
             * Take this opportunity to remove from the table entries
             * whose weak references have been cleared.
             */
            while ((entry = (LoaderEntry) refQueue.poll()) != null) {
                if (!entry.removed) {   // ignore entries removed below
                    loaderTable.remove(entry.key);
                }
            }

            /*
             * Look up the codebase URL path and parent class loader pair
             * in the table of RMI class loaders.
             */
            LoaderKey key = new LoaderKey(urls, parent);
            entry = loaderTable.get(key);

            if (entry == null || (loader = entry.get()) == null) {
                /*
                 * If entry was in table but it's weak reference was cleared,
                 * remove it from the table and mark it as explicitly cleared,
                 * so that new matching entry that we put in the table will
                 * not be erroneously removed when this entry is processed
                 * from the weak reference queue.
                 */
                if (entry != null) {
                    loaderTable.remove(key);
                    entry.removed = true;
                }

                /*
                 * A matching loader was not found, so create a new class
                 * loader instance for the requested codebase URL path and
                 * parent class loader.  The instance is created within an
                 */
                loader = new Loader(urls, parent);

                /*
                 * Finally, create an entry to hold the new loader with a
                 * weak reference and store it in the table with the key.
                 */
                entry = new LoaderEntry(key, loader);
                loaderTable.put(key, entry);
            }
        }

        return loader;
    }


    /**
     * LoaderKey holds a codebase URL path and parent class loader pair
     * used to look up RMI class loader instances in its class loader cache.
     */
    private static class LoaderKey {

        private URL[] urls;

        private ClassLoader parent;

        private int hashValue;

        public LoaderKey(URL[] urls, ClassLoader parent) {
            this.urls = urls;
            this.parent = parent;

            if (parent != null) {
                hashValue = parent.hashCode();
            }
            for (URL url : urls) {
                hashValue ^= url.hashCode();
            }
        }

        public int hashCode() {
            return hashValue;
        }

        public boolean equals(Object obj) {
            if (obj instanceof LoaderKey) {
                LoaderKey other = (LoaderKey) obj;
                if (parent != other.parent) {
                    return false;
                }
                if (urls == other.urls) {
                    return true;
                }
                if (urls.length != other.urls.length) {
                    return false;
                }
                for (int i = 0; i < urls.length; i++) {
                    if (!urls[i].equals(other.urls[i])) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * LoaderEntry contains a weak reference to an RMIClassLoader.  The
     * weak reference is registered with the private static "refQueue"
     * queue.  The entry contains the codebase URL path and parent class
     * loader key for the loader so that the mapping can be removed from
     * the table efficiently when the weak reference is cleared.
     */
    private static class LoaderEntry extends WeakReference<Loader> {

        public LoaderKey key;

        /**
         * set to true if the entry has been removed from the table
         * because it has been replaced, so it should not be attempted
         * to be removed again
         */
        public boolean removed = false;

        public LoaderEntry(LoaderKey key, Loader loader) {
            super(loader, refQueue);
            this.key = key;
        }
    }


    /**
     * Loader is the actual class of the RMI class loaders created
     * by the RMIClassLoader static methods.
     */
    private static class Loader extends URLClassLoader {

        private String annotation;

        private Loader(URL[] urls, ClassLoader parent) {
            super(urls, parent);

            /*
             * Caching the value of class annotation string here assumes
             * that the protected method addURL() is never called on this
             * class loader.
             */
            annotation = urlsToPath(urls);
        }

        /**
         * Return the string to be annotated with all classes loaded from
         * this class loader.
         */
        public String getClassAnnotation() {
            return annotation;
        }


        @SuppressWarnings("ConstantConditions")
        protected PermissionCollection getPermissions(CodeSource codesource) {
            return null;
        }


        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return super.loadClass(name);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        /**
         * Return a string representation of this loader (useful for
         * debugging).
         */
        public String toString() {
            return super.toString() + "[\"" + annotation + "\"]";
        }
    }
}
