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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;

/**
 * Decorates an X509KeyManager, delegating all methods except those
 * chooseClientAlias, which will always return a specific alias.
 *
 * @author steve_siebert
 */
public class ChosenClientAliasX509KeyManager implements X509KeyManager {

    private final String clientAlias;
    private final X509KeyManager delegate;

    public ChosenClientAliasX509KeyManager(String clientAlias, X509KeyManager delegate) {
        this.clientAlias = clientAlias;
        this.delegate = delegate;
    }

    @Override
    public String chooseClientAlias(String[] strings, Principal[] prncpls,
            Socket socket) {
        return clientAlias;
    }

    @Override
    public String[] getClientAliases(String string, Principal[] prncpls) {
        return delegate.getClientAliases(string, prncpls);
    }

    @Override
    public String[] getServerAliases(String string, Principal[] prncpls) {
        return delegate.getServerAliases(string, prncpls);
    }

    @Override
    public String chooseServerAlias(String string, Principal[] prncpls,
            Socket socket) {
        return delegate.chooseServerAlias(string, prncpls, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String string) {
        return delegate.getCertificateChain(string);
    }

    @Override
    public PrivateKey getPrivateKey(String string) {
        return delegate.getPrivateKey(string);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
