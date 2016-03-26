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
package org.geoint.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.function.Function;
import javax.net.ssl.SSLContext;

/**
 * TODO create the certificates programmatically
 */
public enum TestKeyStore {

    CLIENT("client.jks", "client"),
    SERVER("server.jks", "server"),
    TRUST("truststore.jks", "ca");

    private static final char[] STORE_PASS = "changeit".toCharArray();

    private final String fileName;
    private final String alias;

    private TestKeyStore(String fileName, String alias) {
        this.fileName = fileName;
        this.alias = alias;
    }

    public String getFilePath() {
        return SSLSocketFactoryBuilderTest.class.getClassLoader()
                .getResource("ssl/" + fileName).getPath();
    }

    public InputStream getInputStream() {
        return SSLSocketFactoryBuilderTest.class.getClassLoader()
                .getResourceAsStream("ssl/" + fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public char[] getPassword() {
        return STORE_PASS;
    }

    public String getAliasName() {
        return alias;
    }

    /**
     * Mutual authentication client SSL configuration for testing.
     *
     * @param <T>
     * @param adapter
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static <T> T getDefaultMutualAuthClientConfig(Function<SSLContext, T> adapter)
            throws IOException, GeneralSecurityException {
        return SSLContextBuilder.onBuild(adapter)
                .useJksKeyStore(CLIENT.getInputStream(), CLIENT.getPassword())
                .useCertificate(CLIENT.getAliasName(), CLIENT.getPassword())
                .useJksTrustStore(TRUST.getInputStream(), TRUST.getPassword())
                .build();
    }

    public static <T> T getDefaultServerConfig(Function<SSLContext, T> adapter)
            throws IOException, GeneralSecurityException {
        return SSLContextBuilder.onBuild(adapter)
                .useJksKeyStore(SERVER.getInputStream(), SERVER.getPassword())
                .useCertificate(SERVER.getAliasName())
                .useJksTrustStore(TRUST.getInputStream(), TRUST.getPassword())
                .build();
    }

}
