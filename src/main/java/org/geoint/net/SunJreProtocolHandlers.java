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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing convenience for those using the sun/oracle/openjdk
 * JRE.
 *
 * @author steve_siebert
 */
public class SunJreProtocolHandlers {

    private static final String[] JRE_HANDLERS = {
        "sun.net.www.protocol.file.Handler",
        "sun.net.www.protocol.ftp.Handler",
        "sun.net.www.protocol.http.Handler",
        "sun.net.www.protocol.https.Handler",
        "sun.net.www.protocol.jar.Handler",
        "sun.net.www.protocol.mailto.Handler",
        "sun.net.www.protocol.netdoc.Handler"
    };

    public static boolean isJreHandlersAvailable() {
        try {
            Class.forName(JRE_HANDLERS[0]);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    public static void registerJreHandlers() {

        URLContextStreamHandlerFactory shf
                = URLContextStreamHandlerFactory.INSTANCE;
        //TODO make this dynamic if at all possible
        Arrays.stream(JRE_HANDLERS)
                .forEach((cn) -> {
                    try {
                        Class<?> clazz = Class.forName(cn);
                        if (URLStreamHandler.class.isAssignableFrom(clazz)) {
                            Class<? extends URLStreamHandler> handlerClazz
                                    = (Class<? extends URLStreamHandler>) clazz;

                            String[] namespace = clazz.getPackage().getName().split("\\.");
                            String protocol = namespace[namespace.length - 1];
                            shf.registerHandler(protocol, () -> {
                                try {
                                    return handlerClazz.newInstance();
                                } catch (InstantiationException | IllegalAccessException ex) {
                                    //nom nom nom
                                    return null;
                                }
                            });
                        }
                    } catch (ClassNotFoundException ex) {
                        //swallow exception
                    }
                });

        //uses the sun protocol naming convention to detect and load the 
        //protocol handlers provided by the jre
//        Arrays.stream(Package.getPackages())
//                .filter((p) -> p.getName().startsWith("sun.net.www.protocol"))
//                .map((p) -> String.join(".", p.getName(), "Handler"))
//                .forEach((cn) -> {
//                    System.out.println("class name: "+cn);
//                    try {
//                        Class<?> clazz = Class.forName(cn);
//                        System.out.println("package name: "+clazz.getPackage().getName());
//                        if (URLStreamHandler.class.isAssignableFrom(clazz)) {
//                            Class<? extends URLStreamHandler> handlerClazz
//                                    = (Class<? extends URLStreamHandler>) clazz;
//                            
//                            String[] namespace = clazz.getPackage().getName().split("\\.");
//                            String protocol = namespace[namespace.length - 1];
//                            registerHandler(protocol, () -> {
//                                try {
//                                    return handlerClazz.newInstance();
//                                } catch (InstantiationException | IllegalAccessException ex) {
//                                    LOGGER.log(Level.SEVERE, "Unable to initialize "
//                                            + "sun-provided protocol handler.", ex);
//                                    return null;
//                                }
//                            });
//                        }
//                    } catch (ClassNotFoundException ex) {
//                        ex.printStackTrace();
//                        //swallow exception
//                    }
//                });
    }
}
