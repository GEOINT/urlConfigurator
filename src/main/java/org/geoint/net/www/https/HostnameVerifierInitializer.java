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

import java.net.URLConnection;
import java.util.function.Predicate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import org.geoint.net.URLConnectionInitializationException;
import org.geoint.net.URLConnectionInitializer;

/**
 * Called to verify the hostname of a URL if the hostname does not match the
 * servers CN or subjectAltName field, as defined by
 * {@link http://tools.ietf.org/search/rfc6125 RFC 6125}.
 * <p>
 * This class only supports HttpsURLConnector instances.
 *
 */
//TODO create a hostname verifier for other SSL connections...URLConnection API isn't really too helpful here
public class HostnameVerifierInitializer implements URLConnectionInitializer {
    
    private final HostnameVerifier verifier;

    /**
     *
     * @param verifier verifier to use
     */
    public HostnameVerifierInitializer(HostnameVerifier verifier) {
        this.verifier = verifier;
    }

    /**
     * Create a verifier testing just the hostname.
     *
     * @param hostnameVerifier
     */
    public HostnameVerifierInitializer(Predicate<String> hostnameVerifier) {
        this((n, s) -> hostnameVerifier.test(n));
    }
    
    @Override
    public void initialize(URLConnection connection)
            throws URLConnectionInitializationException {
        if (!HttpsURLConnection.class.isAssignableFrom(connection.getClass())) {
            return;
        }
        ((HttpsURLConnection) connection).setHostnameVerifier(verifier);
    }
    
}
