package ca.noae.advancementhelper.database.Structures;

public enum requestType {
  CREATE("Q".toCharArray()[0]),
  READ("S".toCharArray()[0]),
  UPDATE("U".toCharArray()[0]),
  DELETE("D".toCharArray()[0]);

  public final char CHAR;

  private requestType(char CHAR) { this.CHAR = CHAR; }
}
