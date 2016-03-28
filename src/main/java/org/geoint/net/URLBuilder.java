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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Fluid interface to construct a URL.
 * <p>
 * This class is not thread safe.
 */
public class URLBuilder {

    private String protocol;
    private String host;
    private Integer port;
    private List<String> path = new LinkedList();
    private List<Parameter> params = new LinkedList();
    private String ref;
    private String userInfo;

    private static final String PATH_SEPARATOR = "/";
    private static final Pattern PATH_SPLITTER
            = Pattern.compile(PATH_SEPARATOR, Pattern.LITERAL);
    private static final String QUERY_SEPARATOR = "&";
    private static final Pattern QUERY_SPLITTER
            = Pattern.compile(QUERY_SEPARATOR, Pattern.LITERAL);
    private static final String PARAM_SEPARATOR = "=";
    private static final Pattern PARAM_SPLITTER
            = Pattern.compile(PARAM_SEPARATOR, Pattern.LITERAL);

    public URLBuilder() {
    }

    public URLBuilder(String baseUrl) throws MalformedURLException {
        this(new URL(baseUrl));
    }

    public URLBuilder(URL baseUrl) {
        withProtocol(baseUrl.getProtocol())
                .withHost(baseUrl.getHost())
                .withPort(baseUrl.getPort())
                .withPath(baseUrl.getPath())
                .withQuery(baseUrl.getQuery())
                .withRef(baseUrl.getRef());
    }

    /**
     * Sets the URL protocol (scheme).
     *
     * @param protocol protocol
     * @return this builder (fluid interface)
     */
    public URLBuilder withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Sets the host of the URL.
     *
     * @param host host
     * @return this builder (fluid interface)
     */
    public URLBuilder withHost(String host) {
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
    public URLBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Uses the default port of the URL.
     *
     * @return this builder (fluid interface)
     */
    public URLBuilder withDefaultPoort() {
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
    public URLBuilder withPath(String path) {
        if (path != null) {
            return withPath(PATH_SPLITTER.split(path));
        }
        this.path.clear();
        return this;
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
    public URLBuilder withPath(String... path) {
        this.path.clear();
        return appendToPath(path);
    }

    /**
     * Appends the tokens to the URL path.
     *
     * @param tokens path tokens
     * @return this builder (fluid interface)
     */
    public URLBuilder appendToPath(String... tokens) {
        this.path.addAll(Arrays.asList(tokens));
        return this;
    }

    /**
     * Sets the query, removing any parameters that may already be set.
     *
     * @param query url query
     * @return this builder (fluid interface)
     */
    public URLBuilder withQuery(String query) {
        this.params.clear();
        this.parseQuery(query, this::addParameter);
        return this;
    }

    protected void parseQuery(String query,
            BiConsumer<String, String> paramConsumer) {
        if (query == null) {
            return;
        }
        Arrays.stream(QUERY_SPLITTER.split(query))
                .map(PARAM_SPLITTER::split)
                .forEach((p) -> paramConsumer.accept(p[0], p[1]));

    }

    public URLBuilder addParameter(String key, String value) {
        this.params.add(new Parameter(key, value));
        return this;
    }

    public URLBuilder addParameter(Charset charset, String key, String value) {
        this.params.add(new Parameter(key, value, charset));
        return this;
    }

    public URLBuilder addParameters(Charset charset, String key, String... values) {
        Arrays.stream(values).forEach((v) -> this.addParameter(charset, key, v));
        return this;
    }

    public URLBuilder addParameters(String key, String... values) {
        Arrays.stream(values).forEach((v) -> this.addParameter(key, v));
        return this;
    }

    public URLBuilder removeParameter(String key, String value) {
        Iterator<Parameter> iterator = this.params.iterator();
        while (iterator.hasNext()) {
            Parameter p = iterator.next();
            if (p.getKey().contentEquals(key)
                    && p.getValue().contentEquals(value)) {
                iterator.remove();
            }
        }
        return this;
    }

    public URLBuilder removeParameters(String key) {
        Iterator<Parameter> iterator = this.params.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getKey().contentEquals(key)) {
                iterator.remove();
            }
        }
        return this;
    }

    public URLBuilder withRef(String ref) {
        this.ref = ref;
        return this;
    }

    public URL toURL() throws MalformedURLException {
        try {
            return toURI().toURL();
        } catch (URISyntaxException ex) {
            throw new MalformedURLException("Invalid URI syntax. "
                    + ex.getMessage());
        }
    }

    public URI toURI() throws URISyntaxException {
        return new URI(protocol,
                userInfo,
                host,
                (port == null)
                        ? port
                        : -1,
                (path.isEmpty())
                        ? null
                        : String.join(PATH_SEPARATOR, path),
                (params.isEmpty())
                        ? null
                        : params.stream().map(Parameter::toString)
                        .collect(Collectors.joining(QUERY_SEPARATOR)),
                ref);
    }

    public String asString() {
        try {
            return toURI().toString();
        } catch (URISyntaxException ex) {
            return "Invalid URI syntax";
        }
    }

    protected final class Parameter {

        private final String key;
        private final String value;
        private final Charset charset;

        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
            this.charset = StandardCharsets.UTF_8;
        }

        public Parameter(String key, String value, Charset charset) {
            this.key = key;
            this.value = value;
            this.charset = charset;
        }

        public String getKey() {
            return key;
        }

        public String getEncodedKey() {
            try {
                return URLEncoder.encode(key, charset.name());
            } catch (UnsupportedEncodingException ex) {
                try {
                    return URLEncoder.encode(key, Charset.defaultCharset().name());
                } catch (UnsupportedEncodingException ex1) {
                    return key;
                }
            }
        }

        public String getValue() {
            return value;
        }

        public String getEncodedValue() {
            try {
                return URLEncoder.encode(value, charset.name());
            } catch (UnsupportedEncodingException ex) {
                try {
                    return URLEncoder.encode(value, Charset.defaultCharset().name());
                } catch (UnsupportedEncodingException ex1) {
                    return value;
                }
            }
        }

        @Override
        public String toString() {
            return String.format("%s=%s", getEncodedKey(), getEncodedValue());
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode(this.key);
            hash = 79 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Parameter other = (Parameter) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

    }
}
