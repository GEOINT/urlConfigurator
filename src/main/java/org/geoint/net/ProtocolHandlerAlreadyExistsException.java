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

import java.net.URLStreamHandler;

/**
 * Thrown if an attempt to
 * {@link UrlContextStreamHandlerFactory#registerHandler(String, URLStreamHandler) register}
 * a {@link URLStreamHandler} instance failed because a protocol there is a
 * handler already registered for this protocol.
 *
 * @author steve_siebert
 */
public class ProtocolHandlerAlreadyExistsException extends RuntimeException {

    private final String protocol;
    private final Class<? extends URLStreamHandler> registeredHandler;
    private final Class<? extends URLStreamHandler> conflictingHandler;

    public ProtocolHandlerAlreadyExistsException(String protocol,
            Class<? extends URLStreamHandler> registeredHandler,
            Class<? extends URLStreamHandler> conflictingHandler) {
        super(String.format("Handler '%s' cannot be registered for protocol '%s', "
                + "'%s' is already registered.",
                conflictingHandler.getName(),
                protocol,
                registeredHandler.getName()));
        this.protocol = protocol;
        this.registeredHandler = registeredHandler;
        this.conflictingHandler = conflictingHandler;
    }

    public String getProtocol() {
        return protocol;
    }

    public Class<? extends URLStreamHandler> getRegisteredHandler() {
        return registeredHandler;
    }

    public Class<? extends URLStreamHandler> getConflictingHandler() {
        return conflictingHandler;
    }

}
