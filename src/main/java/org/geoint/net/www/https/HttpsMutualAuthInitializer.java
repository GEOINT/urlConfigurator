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
package org.geoint.net.www.https;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URLConnection;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.geoint.net.URLConnectionInitializationException;
import org.geoint.net.URLConnectionInitializer;
import org.geoint.net.ssl.SSLContextBuilder;

/**
 * Configures a URLConnection for mutual authentication with X.509 certificates.
 * <p>
 * This initializer currently only supports the HTTPS protocol, configuring
 * mutual authentication for implementations of HttpsURLConnection.
 * <p>
 * HttpsMutualAuthInitializer ensures that there is a {@link CookieHandler}
 * configured, otherwise configures the {@link CookieManager JVM default}. If
 * you wish to use a custom CookieHandler you should set this up before
 * initializing this class.
 *
 * @author steve_siebert
 */
public class HttpsMutualAuthInitializer implements URLConnectionInitializer {

    private final SSLSocketFactory sslSocketFactory;

    private static final Logger LOGGER
            = Logger.getLogger(HttpsMutualAuthInitializer.class.getName());

    public HttpsMutualAuthInitializer(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager());
        }
    }

    /**
     * Fluid API to construct a SSLSocketFactory used by this initializer.
     *
     * @return builder
     */
    public static SSLContextBuilder builder() {
        return SSLContextBuilder.onBuild(
                (c) -> new HttpsMutualAuthInitializer(c.getSocketFactory()));
    }

    @Override
    public void initialize(URLConnection connection)
            throws URLConnectionInitializationException {

        if (!HttpsURLConnection.class.isAssignableFrom(connection.getClass())) {
            LOGGER.warning(String.format("Unable to configure mutual "
                    + "autentication on URLConnection type '%s'.  This warning "
                    + "may indicate URLProtocolRegistry is misconfigured.",
                    connection.getClass().getName()));
            return;
        }

        ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
    }

}
