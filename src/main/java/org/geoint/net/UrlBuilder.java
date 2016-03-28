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

import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Fluid interface to construct a URL.
 * <p>
 * This class is not thread safe.
 */
public class UrlBuilder {

    private String protocol;
    private String host;
    private Integer port;
    private List<String> path;
    private List<Entry<String, String>> params;
    private String ref;
    private String userInfo;
    private URLStreamHandler handler;

    private static final Pattern PATH_SEPARATOR
            = Pattern.compile("/", Pattern.LITERAL);

    public UrlBuilder() {
    }

    public UrlBuilder(URL baseUrl) {

    }

    /**
     * Sets the URL protocol (scheme).
     *
     * @param protocol protocol
     * @return this builder (fluid interface)
     */
    public UrlBuilder withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Sets the host of the URL.
     *
     * @param host host
     * @return this builder (fluid interface)
     */
    public UrlBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port of the URL.
     * <p>
     * If not port is set, the default port for the protocol will be used. To
     * unset the port, and use the default port for the protocol, see
     * {@link #useDefaultPort()}.
     *
     * @param port port
     * @return this builder (fluid interface)
     */
    public UrlBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Uses the default port of the URL.
     *
     * @return this builder (fluid interface)
     */
    public UrlBuilder withDefaultPoort() {
        this.port = null;
        return this;
    }

    /**
     * Sets the path, removing any existing path.
     * <p>
     * To append to the path, see {@link #appendToPath(java.lang.String...).
     *
     * @param path path
     * @return this builder (fluid interface)
     */
    public UrlBuilder withPath(String path) {
        return withPath(PATH_SEPARATOR.split(path));
    }

    /**
     * Sets the path by sequentially constructing the directory hierarchy from
     * the array, removing any existing path.
     * <p>
     * To append to the path, see {@link #appendToPath(java.lang.String...) }
     *
     * @param path path
     * @return this builder (fluid interface)
     */
    public UrlBuilder withPath(String... path) {
        this.path.clear();
        return appendToPath(path);
    }

    /**
     * Appends the tokens to the URL path.
     * 
     * @param tokens path tokens 
     * @return this builder (fluid interface)
     */
    public UrlBuilder appendToPath(String... tokens) {
        this.path.addAll(Arrays.asList(tokens));
        return this;
    }

    /**
     * Sets the query, removing any parameters that may already be set.
     * 
     * @param query url query
     * @return this builder (fluid interface)
     */
    public UrlBuilder withQuery(String query) {
        
    }

    public UrlBuilder addParameter(String key, String value) {

    }

    public UrlBuilder addParameters(String key, String... vlaues) {

    }

    public UrlBuilder removeParameter(String key, String value) {

    }

    public UrlBuilder removeParameters(String key) {

    }

    public UrlBuilder withRef(String ref) {

    }

    public UrlBuilder useHandler(URLStreamHandler handler) {

    }

    public URL toURL() {

    }

    public URI toURI() {

    }

    public String asString() {

    }
}
