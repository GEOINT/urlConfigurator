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
import java.security.KeyStore;
import java.util.function.Function;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

/**
 * Fluid API to construct an SSLContext, or something that adapts an SSLContext.
 *
 * @author steve_siebert
 * @param <T> class created on build
 */
public class SSLContextBuilder<T> {

    private static final String JKS_STORE_TYPE = "jks";

    private InputStream keyStore;
    private String keyStoreType;
    private char[] keyStorePass;
    private String alias;
    private char[] aliasPass;
    private InputStream trustStore;
    private String trustStoreType;
    private char[] trustStorePass;
    private final Function<SSLContext, T> buildAdapter;

    /**
     * Specify an adapter that receives the SSLContext on successful build
     * return returns the specified type.
     *
     * @param ssfAdapter adapter
     */
    private SSLContextBuilder(Function<SSLContext, T> ssfAdapter) {
        this.buildAdapter = ssfAdapter;
    }

    /**
     * Executes adapter on successful build.
     *
     * @param <T> type to return
     * @param adapter adapter executed on successful SSLContext creation
     * @return adapted type
     */
    public static <T> SSLContextBuilder<T> onBuild(Function<SSLContext, T> adapter) {
        return new SSLContextBuilder(adapter);
    }

    /**
     * Builder which returns the SSLContext on build.
     *
     * @return ssl context
     */
    public static SSLContextBuilder<SSLContext> buildContext() {
        return new SSLContextBuilder((c) -> c);
    }

    /**
     * Returns an SSLSocketFactory on build.
     *
     * @return ssl socket factory
     */
    public static SSLContextBuilder<SSLSocketFactory> buildFactory() {
        return new SSLContextBuilder((c) -> ((SSLContext) c).getSocketFactory());
    }

    /**
     * Returns an SSLServerSocketFactory on build.
     *
     * @return ssl server socket factory
     */
    public static SSLContextBuilder<SSLServerSocketFactory> buildServerFactory() {
        return new SSLContextBuilder((c) -> ((SSLContext) c).getServerSocketFactory());
    }

    public SSLContextBuilder<T> useKeyStore(InputStream in,
            String keyStoreType, char[] password) {
        this.keyStore = in;
        this.keyStoreType = keyStoreType;
        withKeyStorePass(password);
        return this;
    }

    public SSLContextBuilder<T> useKeyStore(InputStream in,
            String keyStoreType) {
        return this.useKeyStore(in, keyStoreType, null);
    }

    public SSLContextBuilder<T> useJksKeyStore(InputStream in) {
        return this.useKeyStore(in, JKS_STORE_TYPE, null);
    }

    public SSLContextBuilder<T> useJksKeyStore(InputStream in,
            char[] password) {
        return this.useKeyStore(in, JKS_STORE_TYPE, password);
    }

    public SSLContextBuilder<T> withKeyStorePass(char[] keyStorePass) {
        this.keyStorePass = keyStorePass;
        return this;
    }

    /**
     * Specifies the keystore certificate, by alias name, to return for all
     * certificate requests.
     *
     * @see ChosenClientAliasX509KeyManager
     * @param certificateAlias alias of certificate to always return
     * @return this builder (fluid interface)
     */
    public SSLContextBuilder<T> useCertificate(
            String certificateAlias) {
        return this.useCertificate(certificateAlias, null);
    }

    /**
     * Specifies the keystore certificate, by alias name, to return for all
     * certificate requests.
     *
     * @see ChosenClientAliasX509KeyManager
     * @param cerficiatePassword certificate password
     * @param certificateAlias alias of certificate to always return
     * @return this builder (fluid interface)
     */
    public SSLContextBuilder<T> useCertificate(
            String certificateAlias, char[] cerficiatePassword) {
        this.alias = certificateAlias;
        this.aliasPass = cerficiatePassword;
        return this;
    }

    public SSLContextBuilder<T> useTrustStore(InputStream in,
            String trustStoreType, char[] trustStorePass) {
        this.trustStore = in;
        this.trustStoreType = trustStoreType;
        withTrustStorePass(trustStorePass);
        return this;
    }

    public SSLContextBuilder<T> useTrustStore(InputStream in,
            String keyStoreType) {
        return this.useTrustStore(in, keyStoreType, null);
    }

    public SSLContextBuilder<T> useJksTrustStore(InputStream in) {
        return this.useTrustStore(in, JKS_STORE_TYPE, null);
    }

    public SSLContextBuilder<T> useJksTrustStore(InputStream in,
            char[] password) {
        return this.useTrustStore(in, JKS_STORE_TYPE, password);
    }

    public SSLContextBuilder<T> withTrustStorePass(char[] trustStorePass) {
        this.trustStorePass = trustStorePass;
        return this;
    }

    public T build()
            throws IOException, GeneralSecurityException {

        SSLContext context = SSLContext.getInstance("SSL");
        context.init(createKeyManagers(), createTrustManagers(), null);
        
        return buildAdapter.apply(context);
    }

    private KeyManager[] createKeyManagers()
            throws GeneralSecurityException, IOException {

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(keyStore, keyStorePass);

        //load KeyManagers like normal, leveraging the system to do the 
        //heavy lifting; then we'll specialize
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, (aliasPass == null) ? keyStorePass : aliasPass);
        KeyManager[] km = kmf.getKeyManagers();

        //decorate any X509KeyManager to always choose the client alias if 
        //this was sepecified
        if (alias != null) {
            for (int i = 0; i < km.length; i++) {
                if (km[i] instanceof X509KeyManager) {
                    km[i] = new ChosenClientAliasX509KeyManager(alias,
                            (X509KeyManager) km[i]);
                }
            }
        }

        return km;
    }

    private TrustManager[] createTrustManagers()
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(trustStoreType);
        ks.load(trustStore, trustStorePass);

        TrustManagerFactory tmFact = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        tmFact.init(ks);
        return tmFact.getTrustManagers();
    }
}
