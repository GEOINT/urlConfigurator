/*
 * Copyright 2016 geoint.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geoint.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * URLStreamHandlerFactory instance which may be used programmatically or may
 * optionally be {@link URLProtocolRegistry#registerWithJvm() registered} as the
 * default stream handler factory for the JVM.
 *
 * @see URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)
 * @author steve_siebert
 */
public class URLProtocolRegistry {

    private final Map<String, URLContextStreamHandler> streamHandlers; //key=protocol
    //TODO replace with a more sophisticated way to filter initializers (ie tree)
    private final Set<URLContextConnectionInitializer> initializers;

    private static final Logger LOGGER
            = Logger.getLogger(URLProtocolRegistry.class.getName());

    public URLProtocolRegistry() {
        streamHandlers = new HashMap<>();
        initializers = new LinkedHashSet<>();
    }

    /**
     * Register this as the JVM "default" StreamHandlerFactory used by URL to
     * create URLConnection instances.
     *
     * @see URL#setURLStreamHandlerFactory(URLStreamHandlerFactory)
     * @throws Error if the application has already set a factory.
     * @throws SecurityException if a security manager exists and its
     * checkSetFactory method doesn't allow the operation.
     */
    public synchronized void registerWithJvm() throws Error, SecurityException {
        boolean register = ProtocolRegistryStreamHandlerFactory.INSTANCE.registry == null;
        ProtocolRegistryStreamHandlerFactory.INSTANCE.registry = this;
        if (register) {
            URL.setURLStreamHandlerFactory(ProtocolRegistryStreamHandlerFactory.INSTANCE);
        }
    }

    /**
     * Register a {@link URLStreamHandler handler} for the specified protocol.
     *
     * @param protocol supported protocol
     * @param handler handler for protocol
     * @throws ProtocolHandlerAlreadyExistsException if there is a handler
     * already registered for this protocol
     * @see #replaceHandler(String, URLStreamHandler)
     */
    public void registerHandler(String protocol, Supplier<? extends URLStreamHandler> handler)
            throws ProtocolHandlerAlreadyExistsException {
        synchronized (streamHandlers) {
            if (streamHandlers.containsKey(protocol)) {
                throw new ProtocolHandlerAlreadyExistsException(protocol,
                        streamHandlers.get(protocol).getDelegateHandler().getClass(),
                        handler.get().getClass());
            }
            streamHandlers.put(protocol, new URLContextStreamHandler(handler));
        }
    }

    /**
     * Register a {@link URLStreamHandler handler} for the specified protocol,
     * replacing the handler currently registered, if exists.
     *
     * @param protocol supported protocol
     * @param handler handler for protocol
     */
    public void replaceHandler(String protocol,
            Supplier<? extends URLStreamHandler> handler) {
        synchronized (streamHandlers) {
            streamHandlers.put(protocol, new URLContextStreamHandler(handler));
        }
    }

    /**
     * Removes a URLStreamHandler for the specified protocol, making the factory
     * unable to resolve for this protocol.
     *
     * @param protocol protocol handler to remove
     */
    public void removeHandler(String protocol) {
        synchronized (streamHandlers) {
            streamHandlers.remove(protocol);
        }
    }

    /**
     * Adds a URLConnection initializer which will be called for all
     * URLConnection instances.
     *
     * @param initializer initializer
     */
    public void addInitializer(URLConnectionInitializer initializer) {
        addContextInitializer(new URLContextConnectionInitializer(initializer));
    }

    /**
     * Adds a URLConnection initializer that will be called for all URLs using
     * the specified protocol/scheme.
     *
     * @param initializer initializer
     * @param protocol protocol defined by the URL
     */
    public void addInitializer(
            URLConnectionInitializer initializer, String protocol) {
        addInitializer(initializer,
                (u) -> u.getProtocol().equalsIgnoreCase(protocol.toLowerCase()));
    }

    /**
     * Adds a URLConnection initializer that will be called for all URLs with
     * the specified protocol AND for the specified hostname.
     *
     * @param initializer initializer
     * @param protocol protocol defined by the URL
     * @param hostname hostname defined by the URL
     */
    public void addInitializer(URLConnectionInitializer initializer,
            String protocol, String hostname) {
        addInitializer(initializer,
                (u) -> u.getProtocol().equalsIgnoreCase(protocol.toLowerCase())
                && u.getHost().equalsIgnoreCase(hostname)
        );
    }

    /**
     * Adds a URLConnection initializer that will be called for all URLs with
     * the specified protocol, hostname, and port.
     *
     * @param initializer initializer
     * @param protocol protocol defined by the URL
     * @param hostname hostname defined by the URL
     * @param port port defined by the URL
     */
    public void addInitializer(URLConnectionInitializer initializer,
            String protocol, String hostname, int port) {
        addInitializer(initializer,
                (u) -> u.getProtocol().equalsIgnoreCase(protocol.toLowerCase())
                && u.getHost().equalsIgnoreCase(hostname)
                && u.getPort() == port
        );
    }

    /**
     * Adds a URLConnection initializer that will be called for all URLs with
     * the specified protocol, hostname, and is or exists under the provided
     * relative path.
     *
     * @param initializer initializer
     * @param protocol protocol defined by the URL
     * @param hostname hostname defined by the URL
     * @param path relative path
     */
    public void addInitializer(URLConnectionInitializer initializer,
            String protocol, String hostname, String path) {
        addInitializer(initializer,
                (u) -> u.getProtocol().equalsIgnoreCase(protocol.toLowerCase())
                && u.getHost().equalsIgnoreCase(hostname)
                && u.getPath().startsWith(path));
    }

    /**
     * Adds a URLConnection initializer that will be called for all URLs with
     * the specified protocol, hostname, and port, and is or exists under the
     * provided relative path.
     *
     * @param initializer initializer
     * @param protocol protocol defined by the URL
     * @param hostname hostname defined by the URL
     * @param port port defined by the URL
     * @param path relative path
     */
    public void addInitializer(URLConnectionInitializer initializer,
            String protocol, String hostname, int port, String path) {
        addInitializer(initializer,
                (u) -> u.getProtocol().equalsIgnoreCase(protocol.toLowerCase())
                && u.getHost().equalsIgnoreCase(hostname)
                && u.getPort() == port
                && u.getPath().startsWith(path)
        );
    }

    /**
     * Adds a URLConnection initializer that is only used when the URL passes
     * the provided test.
     *
     * @param initializer
     * @param filter
     */
    public void addInitializer(
            URLConnectionInitializer initializer, Predicate<URL> filter) {
        addContextInitializer(
                new URLContextConnectionInitializer(initializer, filter)
        );
    }

    private void addContextInitializer(URLContextConnectionInitializer init) {
        synchronized (initializers) {
            initializers.add(init);
        }
    }

    /**
     * Returns a URLStreamHandler creates/initializes URLConnection instances
     * based on the full URL context, rather than just the URL scheme/protocol.
     *
     * @param protocol url scheme
     * @return stream handler which resolves actual URLStreamHandler from URL
     * context
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return (streamHandlers.containsKey(protocol))
                ? streamHandlers.get(protocol)
                : null;
    }

    /**
     * Manually initialize the URLConnection.
     *
     * @param conn connection to initialize
     * @throws IOException if thrown by initializer
     */
    public void initialize(URLConnection conn) throws IOException {
        for (URLContextConnectionInitializer i : initializers) {
            i.initialize(conn);
        }
    }

    /**
     * Create a URL with a URLStreamHandler returned by this factory.
     *
     * @see URL#URL(String, String, int,String, URLStreamHandler)
     * @param url url as string
     * @return URL that will create initialized URLConnection instances
     * @throws MalformedURLException if thrown by URL
     */
    public URL toUrl(String url) throws MalformedURLException {
        URI uri = URI.create(url);
        URL context = new URL(uri.getScheme(), uri.getHost(), uri.getPort(),
                "", createURLStreamHandler(uri.getScheme()));
        return new URL(context, url);
    }

    /**
     * Contextual stream handler which simply iterates through all the
     * initializers in the sequence they were added to the factory, executing
     * any initializers that are within context of the URL.
     */
    private class URLContextStreamHandler extends URLStreamHandler {

        private final Supplier<? extends URLStreamHandler> handlerSupplier;

        public URLContextStreamHandler(Supplier<? extends URLStreamHandler> handler) {
            this.handlerSupplier = handler;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            URLStreamHandler h = handlerSupplier.get();
            try {
                Method delegateMethod = h.getClass()
                        .getDeclaredMethod("openConnection", URL.class);
                delegateMethod.setAccessible(true);
                URLConnection conn
                        = (URLConnection) delegateMethod.invoke(h, u);
                initialize(conn);
                return conn;
            } catch (NoSuchMethodException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException ex) {
                String msg = String.format("Unable to execute "
                        + "#openConnection(java.net.URL) on delegate handler '%s'.",
                        h.getClass().getName());
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new IOException(msg, ex);
            }
        }

        @Override
        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            URLStreamHandler h = handlerSupplier.get();
            try {
                Method delegateMethod = h.getClass()
                        .getDeclaredMethod("openConnection", URL.class, Proxy.class);
                delegateMethod.setAccessible(true);
                URLConnection conn
                        = (URLConnection) delegateMethod.invoke(h, u, p);
                initialize(conn);
                return conn;
            } catch (NoSuchMethodException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException ex) {
                String msg = String.format("Unable to execute "
                        + "#openConnection(java.net.URL, java.net.Proxy) on "
                        + "delegate handler '%s'.",
                        h.getClass().getName());
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new IOException(msg, ex);
            }
        }

        private URLStreamHandler getDelegateHandler() {
            return handlerSupplier.get();
        }

    }

    private class URLContextConnectionInitializer {

        private final URLConnectionInitializer delegate;
        private final Predicate<URL> filter;

        /**
         * Constructs an initializer that is applied to all URL context.
         *
         * @param delegate
         */
        public URLContextConnectionInitializer(URLConnectionInitializer delegate) {
            this.delegate = delegate;
            this.filter = (u) -> true;
        }

        /**
         * Constructs an initializer that applied to only those connections
         * passing the URL predicate.
         *
         * @param delegate initializer
         * @param filter URL context filter
         */
        public URLContextConnectionInitializer(URLConnectionInitializer delegate,
                Predicate<URL> filter) {
            this.delegate = delegate;
            this.filter = filter;
        }

        public void initialize(URLConnection connection)
                throws URLConnectionInitializationException, IOException {
            if (filter.test(connection.getURL())) {
                try {
                    delegate.initialize(connection);
                } catch (IncompleteConnectionInitializationException ex) {
                    LOGGER.log(Level.WARNING, String.format("URLConnection "
                            + "initialization was incomplete for URL '%s'.",
                            connection.getURL().toString()), ex);
                } catch (FatalConnectionInitializationException ex) {
                    String msg = String.format("URLConnection "
                            + "initialization failed for URL '%s'.",
                            connection.getURL().toString());

                    LOGGER.log(Level.WARNING, msg, ex);
                    throw new IOException(msg, ex);
                }
            }
        }

    }

    /**
     * Proxy StreamHandlerFactory allowing different instances of
     * ProtocolRegistry to be swapped out at runtime, sidestepping the JVM
     * limitation of registering at most one StreamHandlerFactory for the life
     * of the JVM.
     */
    private enum ProtocolRegistryStreamHandlerFactory implements URLStreamHandlerFactory {

        INSTANCE;

        private volatile URLProtocolRegistry registry;

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            return registry.createURLStreamHandler(protocol);
        }

    }

}
