package ca.noae.advancementhelper.database;

import ca.noae.advancementhelper.database.Helpers.ObjectHelper;
import ca.noae.advancementhelper.database.Structures.AHRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.logging.Logger;
import javax.net.ssl.*;

public class mTLSDatabase {

  private static final String KEYSTORE_LOCATION = "databaseKeystore.jks";
  private static final String KEYSTORE_PASSWORD = "databaseKeystore";
  private static final String TRUSTSTORE_LOCATION = "databaseTruststore.jks";
  private static final String TRUSTSTORE_PASSWORD = "databaseKeystore";
  private static final Logger LOGGER =
      Logger.getLogger(mTLSDatabase.class.getName());

  public static void main(String[] args) {

    ObjectMapper mapper = new ObjectMapper();
    new ObjectHelper(mapper);
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    try {
      // Initialize SSL context
      SSLContext sslContext = SSLContext.getInstance("TLS");

      // Initialize key manager factory
      KeyManagerFactory keyManagerFactory =
          KeyManagerFactory.getInstance("SunX509");
      KeyStore keyStore = KeyStore.getInstance("JKS");
      try (InputStream dKInputStream =
               classloader.getResourceAsStream(KEYSTORE_LOCATION)) {
        keyStore.load(dKInputStream, KEYSTORE_PASSWORD.toCharArray());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
      }

      // Initialize trust manager factory
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance("SunX509");
      KeyStore trustStore = KeyStore.getInstance("JKS");
      try (InputStream dTInputStream =
               classloader.getResourceAsStream(TRUSTSTORE_LOCATION)) {
        trustStore.load(dTInputStream, TRUSTSTORE_PASSWORD.toCharArray());
        trustManagerFactory.init(trustStore);
      }

      // Creating an SSL server socket
      sslContext.init(keyManagerFactory.getKeyManagers(),
                      trustManagerFactory.getTrustManagers(), null);
      SSLServerSocketFactory sslServerSocketFactory =
          sslContext.getServerSocketFactory();
      SSLServerSocket sslServerSocket =
          (SSLServerSocket)sslServerSocketFactory.createServerSocket(8001);
      sslServerSocket.setNeedClientAuth(true); // Require client authentication

      try {
        while (true) {
          LOGGER.info("Waiting for server connection...\n");

          // Accepting client connections
          SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();
          LOGGER.info("server connected!\n");

          // Receiving and responding to client messages
          InputStream inputStream = sslSocket.getInputStream();
          OutputStream outputStream = sslSocket.getOutputStream();
          try (BufferedReader bufferedReader =
                   new BufferedReader(new InputStreamReader(inputStream))) {
            String message = bufferedReader.readLine();
            message = message.replace("\\\\n", "");
            LOGGER.info(message);
            AHRequest receivedRequest =
                mapper.readValue(message, AHRequest.class);
            LOGGER.info("Received" + receivedRequest.getRequestChar() +
                        " request from server: " + message + "\n");
            LOGGER.info("[] Received token is " +
                        receivedRequest.getPayload().getUserToken());
            String response = "Hello server";
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
    } catch (IOException | NoSuchAlgorithmException | KeyStoreException |
             CertificateException | UnrecoverableKeyException |
             KeyManagementException e) {
      LOGGER.severe("An error occurred: " + e.getMessage());
    }
  }
}
