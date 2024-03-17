package ca.noae.advancementhelper.database.Structures;

import java.io.Serializable;

public class Payload implements Serializable {
  static final long serialVersionUID = 1L;

  String userToken;
  String payloadMessage;

  public Payload(String uT, String pM) {
    this.userToken = uT;
    this.payloadMessage = pM;
  }

  public String getUserToken() { return this.userToken; }

  public String getPayloadMessage() { return this.payloadMessage; }
};