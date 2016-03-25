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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author steve_siebert
 */
public class SSLSocketFactoryBuilderTest {

    //TODO create the certificates programmatically
    private static final char[] STORE_PASS = "changeit".toCharArray();
    private static final String CLIENT_STORE_NAME = "testClient.jks";
    private static final String CLIENT_ALIAS = "testClientAlias";
    private static final String SERVER_STORE_NAME = "testServer.jks";
    private static final String SERVER_ALIAS = "testServerAlias";
    private static final String TRUST_STORE_NAME = "ca.jks";

    /**
     * Test mutual SSL mutual authentication where the client certificate is
     * specified.
     *
     * @throws Exception
     */
    @Test
    public void testMutualAuthSpecifiedClientCert() throws Exception {

        final int serverPort = 11111; //TODO ensure port availability

        //start server
        CerfiticateSnitchHandshakeListener serverCertListener
                = new CerfiticateSnitchHandshakeListener();
        CerfiticateSnitchHandshakeListener clientCertListener
                = new CerfiticateSnitchHandshakeListener();
        MutualAuthServer server
                = new MutualAuthServer(serverPort, serverCertListener);
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();

        //test client
        SSLSocket socket = null;
        try {
            SSLSocketFactory clientSSF = SSLContextBuilder.buildFactory()
                    .useJksKeyStore(getTestKeyStoreStream(CLIENT_STORE_NAME), STORE_PASS)
                    .useCertificate(CLIENT_ALIAS, STORE_PASS)
                    .useJksTrustStore(getTestKeyStoreStream(TRUST_STORE_NAME), STORE_PASS)
                    .build();
            socket = (SSLSocket) clientSSF.createSocket("localhost", serverPort);
            socket.setUseClientMode(true);
            
            socket.addHandshakeCompletedListener(clientCertListener);
            socket.startHandshake();

            while (clientCertListener.cert == null) {
                Thread.sleep(10);
            }

            System.out.println("here");

            assertTrue(serverCertListener.cert != null);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex2) {
                    throw new IOException(ex2);
                }
            }
            server.run = false;
        }
    }

    private InputStream getTestKeyStoreStream(String keystoreName) {
        return SSLSocketFactoryBuilderTest.class.getClassLoader()
                .getResourceAsStream("ssl/" + keystoreName);
    }

    /**
     * Simple SSL server requiring mutual authentication
     */
    private class MutualAuthServer implements Runnable {

        private final int port;
        private final SSLServerSocketFactory ssf;
        private final HandshakeCompletedListener listener;
        private volatile boolean run = true;

        public MutualAuthServer(int port, HandshakeCompletedListener listener)
                throws IOException, GeneralSecurityException {
            this.port = port;
            this.listener = listener;
            this.ssf = SSLContextBuilder.buildServerFactory()
                    .useJksKeyStore(getTestKeyStoreStream(SERVER_STORE_NAME), STORE_PASS)
                    .useCertificate(SERVER_ALIAS)
                    .useJksTrustStore(getTestKeyStoreStream(TRUST_STORE_NAME), STORE_PASS)
                    .build();
        }

        @Override
        public void run() {

            try {
                SSLServerSocket serverSocket
                        = (SSLServerSocket) ssf.createServerSocket(port);

                serverSocket.setNeedClientAuth(true);

                while (run) {
                    // Wait for a connection request.
                    SSLSocket socket = (SSLSocket) serverSocket.accept();
                    socket.addHandshakeCompletedListener(listener);
                    //  socket.getInputStream().close(); //don't care 
                    Thread process = new Thread(() -> {
                        try (BufferedReader r = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()))) {
                            while (run) {
                                String ln = r.readLine();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    process.setDaemon(true);
                    process.start();
                }
            } catch (IOException ex) {
                //print exception for troubleshooting unit test, otherwise 
                //dont really care because listener will not have access to 
                //client cert details, and so the test will fail
                ex.printStackTrace();
            }
        }
    }

    private class CerfiticateSnitchHandshakeListener implements HandshakeCompletedListener {

        X509Certificate cert;

        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            try {
                cert = (X509Certificate) event.getPeerCertificates()[0];
            } catch (SSLPeerUnverifiedException ex) {
                throw new RuntimeException("Unable to get client certificate.", ex);
            }
        }
    }
}
