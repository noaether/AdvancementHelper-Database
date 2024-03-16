package ca.noae.advancementhelper.database;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Logger;
import javax.net.ssl.*;

public class mTLSDatabase {

    private static final String KEYSTORE_LOCATION = "databaseKeystore.jks";
    private static final String KEYSTORE_PASSWORD = "databaseKeystore";
    private static final String TRUSTSTORE_LOCATION = "databaseTruststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "databaseKeystore";
    private static final Logger LOGGER = Logger.getLogger(mTLSDatabase.class.getName());

    public static void main(String[] args) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try {
            // Initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // Initialize key manager factory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream dKInputStream = classloader.getResourceAsStream(KEYSTORE_LOCATION)) {
                keyStore.load(dKInputStream, KEYSTORE_PASSWORD.toCharArray());
                keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
            }

            // Initialize trust manager factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (InputStream dTInputStream = classloader.getResourceAsStream(TRUSTSTORE_LOCATION)) {
                trustStore.load(dTInputStream, TRUSTSTORE_PASSWORD.toCharArray());
                trustManagerFactory.init(trustStore);
            }

            // Creating an SSL server socket
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(8000);
            sslServerSocket.setNeedClientAuth(true); // Require client authentication

            try {
                while (true) {
                    LOGGER.info("Waiting for server connection...\n");

                    // Accepting client connections
                    SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                    LOGGER.info("Server connected!\n");

                    // Receiving and responding to client messages
                    InputStream inputStream = sslSocket.getInputStream();
                    OutputStream outputStream = sslSocket.getOutputStream();
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String message = bufferedReader.readLine();
                        LOGGER.info("Received message from server: " + message + "\n");
                        String response = "Hello Server";
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