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

import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author steve_siebert
 */
public class URLBuilderTest {

    @Test
    public void testBaseUrlNoPath() throws Exception {
        URLBuilder b = new URLBuilder("http://localhost");
        URL url = b.toURL();
        assertEquals("http", url.getProtocol());
        assertEquals(null, url.getUserInfo());
        assertEquals(-1, url.getPort());
        assertEquals("localhost", url.getHost());
        assertEquals("", url.getPath());
        assertEquals(null, url.getQuery());
        assertEquals(null, url.getRef());
    }

    @Test
    public void testBaseUrlWithPath() throws Exception {
        URLBuilder b = new URLBuilder("http://localhost/test");
        URL url = b.toURL();
        assertEquals("http", url.getProtocol());
        assertEquals(null, url.getUserInfo());
        assertEquals(-1, url.getPort());
        assertEquals("localhost", url.getHost());
        assertEquals("/test", url.getPath());
        assertEquals(null, url.getQuery());
        assertEquals(null, url.getRef());
    }

    @Test
    public void testAppendPath () throws Exception {
        URLBuilder b = new URLBuilder("http://localhost/");
        b.withPath("/test");
        b.appendToPath("foo");
        URL url = b.toURL();
        assertEquals("http", url.getProtocol());
        assertEquals(null, url.getUserInfo());
        assertEquals(-1, url.getPort());
        assertEquals("localhost", url.getHost());
        assertEquals("/test/foo", url.getPath());
        assertEquals(null, url.getQuery());
        assertEquals(null, url.getRef());
    }
    
    @Test
    public void testAddParam () throws Exception {
        URLBuilder b = new URLBuilder("http://localhost/");
        b.addParameter("foo", "bar");
        URL url = b.toURL();
        assertEquals("http", url.getProtocol());
        assertEquals(null, url.getUserInfo());
        assertEquals(-1, url.getPort());
        assertEquals("localhost", url.getHost());
        assertEquals("", url.getPath());
        assertEquals("foo=bar", url.getQuery());
        assertEquals(null, url.getRef());
    }
    
}
