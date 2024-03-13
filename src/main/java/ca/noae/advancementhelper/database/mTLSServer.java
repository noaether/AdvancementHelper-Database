package ca.noae.advancementhelper.database;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Logger;
import javax.net.ssl.*;

public class mTLSServer {

    private static final String KEYSTORE_LOCATION = "serverKeystore.jks";
    private static final String KEYSTORE_PASSWORD = "serverKeystore";
    private static final String TRUSTSTORE_LOCATION = "serverTruststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "serverKeystore";
    private static final Logger LOGGER = Logger.getLogger(mTLSServer.class.getName());

    public static void main(String[] args) {
        try {
            // Initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // Initialize key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream keyStoreFileInputStream = new FileInputStream(KEYSTORE_LOCATION)) {
                keyStore.load(keyStoreFileInputStream, KEYSTORE_PASSWORD.toCharArray());
                keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
            }

            // Initialize trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream trustStoreFileInputStream = new FileInputStream(TRUSTSTORE_LOCATION)) {
                trustStore.load(trustStoreFileInputStream, TRUSTSTORE_PASSWORD.toCharArray());
                trustManagerFactory.init(trustStore);
            }

            // Creating an SSL server socket
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(8000);
            sslServerSocket.setNeedClientAuth(true); // Require client authentication

            try {
                while (true) {
                    LOGGER.info("Waiting for client connection...\n");

                    // Accepting client connections
                    SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                    LOGGER.info("Client connected!\n");

                    // Receiving and responding to client messages
                    InputStream inputStream = sslSocket.getInputStream();
                    OutputStream outputStream = sslSocket.getOutputStream();
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String message = bufferedReader.readLine();
                        LOGGER.info("Received message from client: " + message + "\n");
                        String response = "Hello Client";
                        outputStream.write(response.getBytes());
                    } finally {
                        sslSocket.close();
                    }
                }
            } catch (SSLException e) {
                LOGGER.severe("SSLException occurred: " + e.getMessage());
            } finally {
                sslServerSocket.close();
            }
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            LOGGER.severe("An error occurred: " + e.getMessage());
        }
    }
}