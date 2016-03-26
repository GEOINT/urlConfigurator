package org.geoint.net.www.https;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.geoint.net.SunJreProtocolHandlers;
import org.geoint.net.URLProtocolRegistry;
import org.geoint.net.ssl.TestKeyStore;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpsMutualAuthInitializerTest {

    @Test
    public void testHttpsMutualAuthInnitializer() throws Exception {
        final int port = 11113; //TODO ensure port availability

        URLProtocolRegistry reg = new URLProtocolRegistry();
        SunJreProtocolHandlers.registerJreHandlers(reg);
        reg.addInitializer(TestKeyStore.getDefaultMutualAuthClientConfig(HttpsMutualAuthInitializer::new),
                "https", "localhost", port);

        Server server = getHttpsServer(port);
        try {
            server.start();

            URL url = reg.toUrl("https://localhost:" + port);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()))) {
                String statement = reader.readLine();
                assertEquals(TestHandler.STATEMENT, statement);
            }
        } finally {
            if (server.isStarting() || server.isRunning()) {
                server.stop();
            }
        }
    }

    private Server getHttpsServer(int port) throws Exception {
        Server server = new Server();
        server.setHandler(new TestHandler());

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(port);
        httpsConfig.setOutputBufferSize(2048);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory sslFactory = new SslContextFactory();
        sslFactory.setKeyStorePath(TestKeyStore.SERVER.getFilePath());
        sslFactory.setKeyStorePassword(String.valueOf(TestKeyStore.SERVER.getPassword()));
        sslFactory.setKeyManagerPassword(String.valueOf(TestKeyStore.SERVER.getPassword()));
        sslFactory.setTrustStorePath(TestKeyStore.TRUST.getFilePath());
        sslFactory.setTrustStorePassword(String.valueOf(TestKeyStore.TRUST.getPassword()));
        sslFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        sslConnector.setPort(port);
        server.addConnector(sslConnector);

        return server;
    }

    /**
     * Simple jetty handler just for testing
     */
    private class TestHandler extends AbstractHandler {

        public static final String STATEMENT = "<root>USA!</root>";

        @Override
        public void handle(String string, Request rqst, HttpServletRequest req,
                HttpServletResponse resp) throws IOException, ServletException {
            resp.setContentType("application/xml; charset=utf-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(STATEMENT);
            rqst.setHandled(true);
        }

    }
}
