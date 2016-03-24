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

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Objects;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 * TODO fix test dependency on Sun JRE
 * 
 * @author steve_siebert
 */
public class URLContextStreamHandlerFactoryTest {

    @BeforeClass
    public static void before() {
        SunJreProtocolHandlers.registerJreHandlers();
    }

    @Test
    public void testRegisterJreHandlers() throws Exception {

        if (!SunJreProtocolHandlers.isJreHandlersAvailable()) {
            fail("Unable to test, no handlers available");
        }

        URLContextStreamHandlerFactory shf = URLContextStreamHandlerFactory.INSTANCE;
        assertTrue(Objects.nonNull(shf.createURLStreamHandler("http")));
    }

    @Test
    public void testInitalizerCalled() throws Exception {

        if (!SunJreProtocolHandlers.isJreHandlersAvailable()) {
            fail("Unable to test, no handlers available");
        }

        URLContextStreamHandlerFactory shf
                = URLContextStreamHandlerFactory.INSTANCE;

        CountingInitializer init = new CountingInitializer();
        shf.addInitializer(init);
        URLStreamHandler handler = shf.createURLStreamHandler("file");

        String tmpDirURL = new File(System.getProperty("java.io.tmpdir"))
                .toURI().toURL().toString();

        URL url = new URL(null, tmpDirURL, handler);

        URLConnection conn = url.openConnection();

        assertEquals(1, init.getCount());

    }
    
    @Test
    public void testJvmRegistration () throws Exception {
        
        if (!SunJreProtocolHandlers.isJreHandlersAvailable()) {
            fail("Unable to test, no handlers available");
        }

        URLContextStreamHandlerFactory shf
                = URLContextStreamHandlerFactory.INSTANCE;
        shf.registerWithJvm();
        
        CountingInitializer init = new CountingInitializer();
        shf.addInitializer(init, "http", "localhost");
        
        URL url = new URL("http://localhost/");
        URLConnection conn = url.openConnection();
        
        assertEquals(1, init.getCount());
    }

}
